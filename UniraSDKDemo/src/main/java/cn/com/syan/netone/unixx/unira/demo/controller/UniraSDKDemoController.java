/*
 * Project: UniraSDK
 * 
 * @(#) UniraSDKDemoController.java   14-5-15 上午10:54
 *
 * Copyright 2013 Jiangsu Syan Technology Co.,Ltd. All rights reserved.
 * Jiangsu Syan PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cn.com.syan.netone.unixx.unira.demo.controller;

import cn.com.syan.jcee.common.impl.security.SM2BCKeyPair;
import cn.com.syan.jcee.common.impl.security.SM2BCKeyPairGenerator;
import cn.com.syan.netone.unixx.unira.demo.servlet.NetOneConfig;
import cn.com.syan.netone.unixx.unira.sdk.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.spongycastle.util.encoders.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.security.*;
import java.util.*;

/**
 * <p>
 * This class provides...
 * </p>
 *
 * @author Iceberg
 * @version $Revision $Date:14-5-15 上午10:54
 * @since 1.0
 */
@Controller
@RequestMapping("/")
public class UniraSDKDemoController {
    String url = NetOneConfig.getInstance().getOnlineUrl();
    String identityCert = NetOneConfig.getInstance().getIdentityCert();
    String identityPrivateKey = NetOneConfig.getInstance().getIdentityPrivateKey();
    String raID = NetOneConfig.getInstance().getRAID();

    @RequestMapping("/x")
    public String index(ModelMap modelMap, HttpServletRequest request) {
        modelMap.put("base", request.getContextPath());
        System.out.println("base=" + modelMap.get("base").toString());

        return "onestepissue";
    }

    /**
     * 默认页面.step1
     *
     * @param modelMap
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/index")
    public String test(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        modelMap.put("base", request.getContextPath());

        return "process";
    }

    /**
     * 注册并审核示例.step2
     *
     * @param modelMap
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/process")
    public String process(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        modelMap.put("base", request.getContextPath());
        String name = request.getParameter("name");
//        String ou = request.getParameter("ou");

        try {
            UniraClientOnlineRA ucoRA = new UniraClientOnlineRA(url, identityCert, identityPrivateKey);

            //根据RA ID获得管理员在Unira中可管辖的RA对象
            UniraResponseRA ura = ucoRA.get(raID);

            UniraClientOnlineClient ucoClient = new UniraClientOnlineClient(url, identityCert, identityPrivateKey);

            //本例中，证书模板中的C项默认值为CN，且不可更改，所以不需要在这里设置C
            User user1 = new User(name);

            List<Map> clientMap1 = this.generateClient(ura, user1);

            //注册用户信息，注册成功后，状态为“已注册待审核”
            UniraResponseClient urClient1 = ucoClient.add(ura.getId(), clientMap1);

            //如果注册成功
            if (!urClient1.isFailed()) {
                //获得注册用户的client id ，client id 将伴随证书声明周期的整个过程，请注意保存
                String user1ClientId = urClient1.getId();
                System.out.println("注册成功，ClientID: " + user1ClientId + ", 现在状态是【已注册待审核】");
                //可以直接审核通过
                UniraResponse user1Response = ucoClient.approve(user1ClientId);
                if (!user1Response.isFailed()) {
                    System.out.println("审核成功，ClientID: " + user1ClientId + ", 现在状态是【已审核待签发】");
                    request.getSession().setAttribute("clientId", user1ClientId);

                    modelMap.put("client_id", user1ClientId);
                    modelMap.put("userName", name);
                }
            }else{
                modelMap.put("error_msg",urClient1.getErrorMessages());
            }

        } catch (Exception e) {
			modelMap.put("error_msg",e.getMessage());
            e.printStackTrace();
        }

        return "issue";
    }

    /**
     * 签发证书,pkcs10->install .step3
     *
     * @param modelMap
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/issue")
    public String issue(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        modelMap.put("base", request.getContextPath());
        Map<String, Object> errorMap = new HashMap<String, Object>();

        //这里的   clientId 是每张证书的ID, 这里仅仅做演示，请根据应用系统的业务逻辑来处理
        String clientId = (String) request.getSession().getAttribute("clientId");
        modelMap.put("clientid", clientId);
        try {
            UniraClientOnlineClient proxy = new UniraClientOnlineClient(url, identityCert, identityPrivateKey);

            String action = request.getParameter("action");
            if ("pkcs10".equals(action)) {
                UniraResponseGencsr gencsr = proxy.issueGencsr(clientId, "csp");

                if (gencsr.isFailed()) {
                    errorMap.put("error", "failed to gen csr");
                    this.writeBack(request, response, new Gson().toJson(errorMap));

                    return null;
                }

                String ajaxResp = gencsr.generateAjaxResponse();
                this.writeBack(request, response, ajaxResp);

                return null;
            } else if ("install".equals(action)) {
				
				 //双证
                String doubleCert = request.getParameter("doubleCert");
                boolean doublecert = Boolean.valueOf(doubleCert);
                Enumeration enu = request.getParameterNames();
                String paraName;
                while (enu.hasMoreElements()) {
                    paraName = (String) enu.nextElement();
                    System.out.println(paraName + "=" + request.getParameter(paraName));
                }

                //默认1年有效期
                UniraResponseCrt crt = proxy.issueAjax(clientId, "1y", request, doublecert);

                if (crt.isFailed()) {
                    errorMap.put("error", "failed to issue ajax");
                    this.writeBack(request, response, new Gson().toJson(errorMap));

                    return null;
                }

                String crtAjaxResponse = crt.generateAjaxResponse();
                System.out.println(crtAjaxResponse);
                this.writeBack(request, response, crtAjaxResponse);

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "issue";
    }

    private void writeBack(HttpServletRequest request, HttpServletResponse response, String responseJson) throws Exception {
        System.out.println(responseJson);
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.write(responseJson);
        writer.flush();
    }


    @RequestMapping("/one")
    public String oneStepIssue(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {

        modelMap.put("base", request.getContextPath());
        System.out.println("base=" + modelMap.get("base").toString());

        return "onestepissue";
    }


    /**
     * 一步签发
     *
     * @param modelMap
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/onestepissue")
    public String issueOneStep(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        modelMap.put("base", request.getContextPath());
        Map errorMap = new HashMap();
        //url     identityCert            identityPrivateKey 请根据实际的真实证书来设置

        //这里的   clientId 是每张证书的ID, 这里仅仅做演示，请根据应用系统的业务逻辑来处理

        System.out.println("++++++++++++++++++++++++++++++++++++++");
        String action = request.getParameter("action");
        String clientId = (String) request.getSession().getAttribute("clientId");

        System.out.println("action : " + action);
        System.out.println("client id: " + clientId);

        if (clientId == null) {
            try {
                errorMap.put("error", "client id is null");
                Gson gson = new Gson();
                String responseJson = gson.toJson(errorMap);
                System.out.println(responseJson);
                PrintWriter writer = response.getWriter();
                writer.write(responseJson);
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }


        try {
            UniraClientOnlineClient proxy = new UniraClientOnlineClient(url, identityCert, identityPrivateKey);


            if ("pkcs10".equals(action)) {
                UniraResponseGencsr gencsr = proxy.issueGencsr(clientId, "csp");
                String ajaxResp = gencsr.generateAjaxResponse();
                System.out.println(ajaxResp);
                response.setContentType("text/plain");
                PrintWriter writer = response.getWriter();
                writer.write(ajaxResp);
                writer.flush();
                return null;
            } else if ("install".equals(action)) {
                UniraResponseCrt crt = proxy.issueAjax(clientId, "1y", request, false);

                String crtAjaxResponse = crt.generateAjaxResponse();

                System.out.println(crtAjaxResponse);
                response.setContentType("text/plain");
                PrintWriter writer = response.getWriter();
                writer.write(crtAjaxResponse);
                writer.flush();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "issue";
    }

    private List<Map> generateClient(UniraResponseRA urRA, User user) {

        //  shortNameOfName   和 shortNameOfOU 这两个字段，必须和在UniCA中设置的证书模板中的短名一致,字段的数量和名称需和CA管理员核对匹配无误
        String shortNameOfName = "s_name";
//        String shortNameOfOU = "s_ou";
        List<UniraResponseEntity> entitySet = urRA.getEntitySet();

        Map entity;
        List<Map> enList = new ArrayList<Map>();
        String shortName = null;
        for (UniraResponseEntity en : entitySet) {
            shortName = en.getShortName();
            if (shortName != null) {
                if (en.getShortName().equals(shortNameOfName)) {
                    en.setValue(user.getName());
                }
//                else if (en.getShortName().equals(shortNameOfOU)) {
//                    en.setValue(user.getOU());
//                }
            }
            entity = new HashMap();

            entity.put("name", en.getName());
            entity.put("value", en.getValue());

            enList.add(entity);
        }

        return enList;
    }

    class User {
        String name;
//        String ou;


        public User(String name) {
            this.name = name;
//            this.ou = ou;
        }

        public String getName() {
            return this.name;
        }

//        public String getOU() {
//            return this.ou;
//        }

    }

    private String genKeyPair() {
        KeyPair keyPair = null;
        String pubKey = null;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024, new SecureRandom());

            keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            pubKey = new String(Base64.encode(publicKey.getEncoded()));
            System.out.println(pubKey);
            System.out.println(new String(Base64.encode(privateKey.getEncoded())));


            SM2BCKeyPair sm2BCKeyPair = SM2BCKeyPairGenerator.generateKeyPair();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return pubKey;
    }

    @RequestMapping(value="/freeze",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String freeze(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        modelMap.put("base", request.getContextPath());
        Map<String, Object> errorMap = new HashMap<String, Object>();

        String crt = request.getParameter("crt");
        String clientId = (String) request.getSession().getAttribute("clientId");
        try {
            UniraClientOnlineCrt proxy = new UniraClientOnlineCrt(url, identityCert, identityPrivateKey);

            UniraResponse uniraResponse = proxy.freeze(crt);
            if (uniraResponse.isFailed()) {
                errorMap.put("status", 1);
                errorMap.put("error", uniraResponse.getErrorMessages().toString());

                return new Gson().toJson(errorMap);
            }

            String ajaxResp = new Gson().toJson(uniraResponse.getResponse());
            return ajaxResp;

        } catch (Exception e) {
            e.printStackTrace();
            errorMap.put("status", 1);
            errorMap.put("error", e.getMessage());
            return new Gson().toJson(errorMap);
        }

    }

    @RequestMapping(value="/unfreeze",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String unfreeze(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        modelMap.put("base", request.getContextPath());
        Map<String, Object> errorMap = new HashMap<String, Object>();

        String crt = request.getParameter("crt");
        String clientId = (String) request.getSession().getAttribute("clientId");
        try {
            UniraClientOnlineCrt proxy = new UniraClientOnlineCrt(url, identityCert, identityPrivateKey);

            UniraResponse uniraResponse = proxy.unfreeze(crt);
            if (uniraResponse.isFailed()) {
                errorMap.put("status", 1);
                errorMap.put("error", uniraResponse.getErrorMessages().toString());

                return new Gson().toJson(errorMap);
            }

            String ajaxResp = new Gson().toJson(uniraResponse.getResponse());
            return ajaxResp;

        } catch (Exception e) {
            e.printStackTrace();
            errorMap.put("status", 1);
            errorMap.put("error", e.getMessage());
            return new Gson().toJson(errorMap);
        }

    }

    @RequestMapping(value="/revoke",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String revoke(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        modelMap.put("base", request.getContextPath());
        Map<String, Object> errorMap = new HashMap<String, Object>();

        String crt = request.getParameter("crt");
        String clientId = (String) request.getSession().getAttribute("clientId");
        try {
            UniraClientOnlineCrt proxy = new UniraClientOnlineCrt(url, identityCert, identityPrivateKey);

            UniraResponse uniraResponse = proxy.revoke(crt);
            if (uniraResponse.isFailed()) {
                errorMap.put("status", 1);
                errorMap.put("error", uniraResponse.getErrorMessages().toString());

                return new Gson().toJson(errorMap);
            }

            String ajaxResp = new Gson().toJson(uniraResponse.getResponse());
            return ajaxResp;

        } catch (Exception e) {
            e.printStackTrace();
            errorMap.put("status", 1);
            errorMap.put("error", e.getMessage());
            return new Gson().toJson(errorMap);
        }

    }

    @RequestMapping(value="/renew",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String renew(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        modelMap.put("base", request.getContextPath());
        Map<String, Object> errorMap = new HashMap<String, Object>();

        String crt = request.getParameter("crt");
        String validity = request.getParameter("validity");
        if(validity==null){
            validity="1y";
        }
        String clientId = (String) request.getSession().getAttribute("clientId");
        try {
            UniraClientOnlineCrt proxy = new UniraClientOnlineCrt(url, identityCert, identityPrivateKey);

            UniraResponse uniraResponse = proxy.renew(crt,validity);
            if (uniraResponse.isFailed()) {
                errorMap.put("status", 1);
                errorMap.put("error", uniraResponse.getErrorMessages().toString());

                return new Gson().toJson(errorMap);
            }

            Map map = (Map)uniraResponse.getResponse().get("data");
            String certificatea = (String)map.get("certificatea");
            if(!StringUtils.isEmpty(certificatea)){
                map.put("certificatea",certificatea.replace("-----BEGIN CERTIFICATE-----","").replace("-----END CERTIFICATE-----",""));
            }
            String certificatee = (String)map.get("certificatee");
            if(!StringUtils.isEmpty(certificatee)){
                map.put("certificatee",certificatee.replace("-----BEGIN CERTIFICATE-----","").replace("-----END CERTIFICATE-----",""));
            }
            String ajaxResp = new Gson().toJson(map);
            return ajaxResp;

        } catch (Exception e) {
            e.printStackTrace();
            errorMap.put("status", 1);
            errorMap.put("error", e.getMessage());
            return new Gson().toJson(errorMap);
        }
    }

}
