/*
 * Project: UniraSDKDemo
 * 
 * @(#) servlet.java   2015/1/22 14:46
 *
 * Copyright 2013 Jiangsu Syan Technology Co.,Ltd. All rights reserved.
 * Jiangsu Syan PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cn.com.syan.netone.unixx.unira.demo.servlet;

import cn.com.syan.netone.unixx.unira.sdk.*;
import cn.com.syan.netone.unixx.unira.sdk.exception.UniraClientException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class provides...
 * </p>
 *
 * @author Iceberg
 * @version $Revision $Date:2015/1/22 14:46
 * @since 1.0
 */
public class IssueOneStepServlet extends HttpServlet {

    String url = NetOneConfig.getInstance().getOnlineUrl();
    String identityCert = NetOneConfig.getInstance().getIdentityCert();
    String identityPrivateKey = NetOneConfig.getInstance().getIdentityPrivateKey();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("utf-8");

        String action = request.getParameter("action");
        try {
            UniraClientOnlineClient proxy = new UniraClientOnlineClient(url, identityCert, identityPrivateKey);

            if ("pkcs10".equals(action)) {
                String userClientId = registerAndApprove();
                if (userClientId != null) {
                    request.getSession().setAttribute("userClientId", userClientId);
                    UniraResponseGencsr gencsr = proxy.issueGencsr(userClientId, "csp");
                    String ajaxResp = gencsr.generateAjaxResponse();
                    response.setContentType("text/plain");
                    PrintWriter writer = response.getWriter();
                    writer.write(ajaxResp);
                    writer.flush();
                    writer.close();
                } else {
                    response.setContentType("text/plain");
                    PrintWriter writer = response.getWriter();
                    writer.write("failed to add client to ra");
                    writer.flush();
                    writer.close();
                }
            } else if ("install".equals(action)) {
                String userClientId = (String) request.getSession().getAttribute("userClientId");
                UniraResponseCrt crt = proxy.issueAjax(userClientId, "1y", request, false);

                String crtAjaxResponse = crt.generateAjaxResponse();
                request.getSession().removeAttribute("userClientId");
                response.setContentType("text/plain");
                PrintWriter writer = response.getWriter();
                writer.write(crtAjaxResponse);
                writer.flush();
            } else {
                response.setContentType("text/plain");
                PrintWriter writer = response.getWriter();
                writer.write("invalid action: " + action);
                writer.flush();
                writer.close();

            }
        } catch (UniraClientException e) {
            e.printStackTrace();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    private String registerAndApprove() throws IOException {
        String userClientId = null;
        try {
            UniraClientOnlineRA ucoRA = new UniraClientOnlineRA(url, identityCert, identityPrivateKey);
            System.out.println("url: " + url);

            String raID = "";
            List<UniraResponseRA> raList = ucoRA.xlist();

            if (raList == null || raList.size() < 1) {
                throw new Exception("no ra id found.");
            }

            for (UniraResponseRA ra : raList) {
                System.out.println("current available ra id = " + ra.getId());
                raID = ra.getId();
            }

            //如果有多个ra,则取配置文件中的RA ID
            if (raList.size() > 1) {
                raID = NetOneConfig.getInstance().getRAID();
            }

            //根据RA ID获得管理员在Unira中可管辖的RA对象
            System.out.println("preferred RA ID= " + raID);
            UniraResponseRA ura = ucoRA.get(raID);
            UniraClientOnlineClient ucoClient = new UniraClientOnlineClient(url, identityCert, identityPrivateKey);

            //本例中，证书模板中的C项默认值为CN，且不可更改，所以不需要在这里设置C
            String name = "user " + System.currentTimeMillis();
            String ou = "先安科技";
            String city = "深圳市";
            String province = "广东省";

            User user = new User(name, ou, city, province);
            List<Map> clientMap1 = this.generateClient(ura, user);

            //注册用户信息，注册成功后，状态为“已注册待审核”
            UniraResponseClient urClient = ucoClient.add(ura.getId(), clientMap1);
            //如果注册成功
            if (!urClient.isFailed()) {
                //获得注册用户的client id ，client id 将伴随证书声明周期的整个过程，请注意保存
                userClientId = urClient.getId();
                System.out.println("注册成功，ClientID: " + userClientId + ", 现在状态是【已注册待审核】");
                //可以直接审核通过
                UniraResponse userResponse = ucoClient.approve(userClientId);
                if (!userResponse.isFailed()) {
                    System.out.println("审核成功，ClientID: " + userClientId + ", 现在状态是【已审核】");
                }
            } else {
                System.err.println("error: " + urClient.getErrorNo());
            }

        } catch (Exception e) {
            throw new IOException("failed to add client, cause " + e.getMessage(), e);
        }

        return userClientId;
    }

    @SuppressWarnings("unchecked")
    private List<Map> generateClient(UniraResponseRA urRA, User user) {
        //  shortNameOfName   和 shortNameOfOU 这两个字段，必须和在UniCA中设置的证书模板中的短名一致
        //String shortNameOfName = "s_name";
        //String shortNameOfOU = "s_ou";
        //String shortNameOfCity = "s_city";
        //String shortNameOfProvince = "s_province";
        String shortNameOfOrganization = "s_organization";

        //szca
        String shortNameOfName = "s_name";
        String shortNameOfOU = "s_ou";
        String shortNameOfCity = "s_l";
        String shortNameOfProvince = "s_st";
        String shortNameOfC = "s_cn";
        //szca

        List<UniraResponseEntity> entitySet = urRA.getEntitySet();

        Map entity;
        List<Map> enList = new ArrayList<Map>();
        String shortName = null;
        for (UniraResponseEntity en : entitySet) {
            shortName = en.getShortName();

            entity = new HashMap();
            if (shortName != null) {
                if (shortName.equals(shortNameOfName)) {
                    en.setValue(user.getName());
                } else if (shortName.equals(shortNameOfOU)) {
                    en.setValue(user.getOU());
                } else if (shortName.equals(shortNameOfOrganization)) {
                    en.setValue(user.getOU());
                } else if (shortName.equals(shortNameOfCity)) {
                    en.setValue(user.getCity());
                } else if (shortName.equals(shortNameOfProvince)) {
                    en.setValue(user.getProvince());
                } else if (shortName.equals(shortNameOfC)) {
                    en.setValue(user.getC());
                }

                entity.put("name", en.getName());
                entity.put("value", en.getValue());
                enList.add(entity);
            }


        }

        return enList;
    }

    class User {
        String name;
        String ou;
        String province = "";
        String city = "";
        String c = "CN";

        public User(String name, String ou) {
            this.name = name;
            this.ou = ou;
        }

        public User(String name, String ou, String city, String province) {
            this(name, ou);
            this.city = city;
            this.province = province;
        }

        public String getName() {
            return this.name;
        }

        public String getOU() {
            return this.ou;
        }

        public String getProvince() {
            return province;
        }

        public String getCity() {
            return city;
        }

        public String getC() {
            return c;
        }
    }

}

