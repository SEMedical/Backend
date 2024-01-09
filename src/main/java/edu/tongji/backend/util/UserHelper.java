package edu.tongji.backend.util;

import edu.tongji.backend.exception.AuthorityException;
import edu.tongji.backend.service.IProfileService;
import edu.tongji.backend.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;

public class UserHelper {

    public static String checkUser(IUserService userService, IProfileService profileService,HttpServletRequest request)
    {
        try {
            if (request.getHeader("Authorization") == null) {
                return "您尚未登录";
            }
            String token = request.getHeader("Authorization");
            System.out.println(token);
            String user_id = Jwt.parse(token).get("userId").toString();
            System.out.println(user_id + "````");
            String role = Jwt.parse(token).get("userPermission").toString();
            if (userService.getById(user_id) == null) {
                throw new AuthorityException("user doesn't exist");
            } else if (!role.equals("patient")) {
                throw new AuthorityException("user isn't a patient");
            } else if (profileService.getByPatientId(user_id) == null)
                throw new AuthorityException("exception with registration of the user" + user_id);
            return user_id;
        }catch (AuthorityException e){
            throw new AuthorityException(e.getMessage());
        }
    }
}
