package com.project.topt_2fa.totp_based_2fa

import com.google.zxing.qrcode.QRCodeWriter
import com.project.topt_2fa.totp_based_2fa.DAO.UserRepo
import com.project.topt_2fa.totp_based_2fa.Model.User
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.awt.image.BufferedImage
import java.util.*
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession


@Controller
class UserController {

    @Autowired
    private lateinit var userRepo: UserRepo


    @GetMapping("")
    fun viewHomePage(): String {
        return "index"
    }

    @GetMapping("/register")
    fun viewRegister(): String {
        return "register"
    }

    @GetMapping("/login")
    fun viewLogin(): String {
        return "login"
    }

    @GetMapping("/logout")
    fun logout(httpsession: HttpSession): ModelAndView? {
        httpsession.invalidate()
        val modelAndView = ModelAndView()
        modelAndView.viewName = "index"
        return modelAndView
    }

    @RequestMapping(value = ["/save"], method = [RequestMethod.POST])
    fun save(@ModelAttribute user: User2?): ModelAndView? {
        val modelAndView = ModelAndView()
        modelAndView.viewName = "user-data"
        modelAndView.addObject("user", user)
        return modelAndView
    }


    @RequestMapping(value = ["/register-user"], method = [RequestMethod.POST])
    fun registeruser(@RequestParam("email") email:String,@RequestParam("pass") pass:String): ModelAndView? {

        val modelAndView = ModelAndView()
        if (0<userRepo.countByEmailEquals(email))
        {
            modelAndView.viewName = "register"
            modelAndView.addObject("msg","User Already Exist")
            return modelAndView
        }
        val user:User = User()
        user.email=email
        user.pass=pass
        user.secret = GoogleAuthenticator.createRandomSecret()
        val gen:generator = generator(QRCodeWriter())
        val img: BufferedImage = gen.generate("Spring Boot ",email, user.secret!!)
        val bao = ByteArrayOutputStream()
        ImageIO.write(img, "jpg", bao)
        userRepo.save(user)
        val byt = bao.toByteArray()
        modelAndView.viewName = "register-success"
        modelAndView.addObject("qrcode", Base64.getEncoder().encodeToString(byt))
        modelAndView.addObject("email",email)
        return modelAndView
    }


    @RequestMapping(value = ["/login-user"], method = [RequestMethod.POST])
    fun login(httpsession:HttpSession, request: HttpServletRequest, @RequestParam("email") email:String, @RequestParam("pass") pass:String, @RequestParam("token") token:String): ModelAndView? {
        val user: List<User> = userRepo.findByEmailAndPass(email, pass)
        val modelAndView = ModelAndView()
        if (user.size ==1)
        {
            val timestamp = Date(System.currentTimeMillis())
            val code = user.get(0).secret?.let { GoogleAuthenticator(it).generate(timestamp) }
            if (code == token)
            {
                httpsession.invalidate()
                val newhttpsession:HttpSession = request.getSession()
                newhttpsession.setAttribute("email",email)
                modelAndView.viewName = "index"
                return modelAndView
            }
            else
            {
                modelAndView.viewName = "login"
                modelAndView.addObject("tokenmsg","Invalid Token")
                return modelAndView
            }
        }
        else
        {
            modelAndView.viewName = "login"
            modelAndView.addObject("msg","Invalid Email or Password")
            return modelAndView
        }

    }

    @ResponseBody
    @GetMapping("hey")
    fun sayhey(): String {
        return "Hey"
    }
}
class User2 {
    var name: String? = null
    var email: String? = null
}

