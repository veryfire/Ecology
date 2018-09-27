package lic;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

public class M {
    private static LdapContext ldapContext = null;

    public static void main(String[] args) throws UnsupportedEncodingException {
        String url = "ldap://192.168.5.63:636";
        String username = "administrator@veryfire.cn";
        String password = "forgive@h3r";
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            ldapContext = new InitialLdapContext(env, null);

            queryUserInfo("lichun");

            ldapContext.close();

        } catch (NamingException ne) {
            System.out.println(ne);
        }
    }

    public static boolean queryUserInfo(String name) {
        String uid = name.toUpperCase();
        boolean status = false;
        String domain = "CN=Users,DC=veryfire,DC=cn";
        String filter = "(CN=*)";
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {
            // ldapContext.destroySubcontext("CN=lichun,CN=Users,DC=veryfire,DC=cn");
            NamingEnumeration<SearchResult> search = ldapContext.search(domain, filter, constraints);
            while (search.hasMore()) {
                String result = search.next().toString();
                System.out.println(result);
                status = true;
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return status;
    }

    public static boolean addUser(String uid) throws UnsupportedEncodingException {
        if (queryUserInfo(uid)) {
            System.out.println("用户已存在");
            return false;
        }
        BasicAttributes attributes = new BasicAttributes(true);
        BasicAttribute attribute = new BasicAttribute("objectclass");
        attribute.add("top");
        attribute.add("person");
        attribute.add("organizationalPerson");
        attribute.add("user");
        attributes.put(attribute);
        attributes.put("userAccountControl", "512");
        attributes.put("CN", uid);
        attributes.put("Sn", uid);
        attributes.put("sAMAccountName", uid);
        attributes.put("userPrincipalName", uid + "@veryfire.cn");
        attributes.put("displayName", uid);
        attributes.put("profilePath", "\\\\192.168.5.63\\SHARE\\" + uid);
        String passwd = "\"forgive@h3r\"";
        byte[] newPasswd = passwd.getBytes("UTF-16LE");
        attributes.put("unicodePwd", newPasswd);
        String udn = "CN=" + uid + ",CN=Users,DC=veryfire,DC=cn";
        try {
            ldapContext.createSubcontext(udn, attributes);
            System.out.println("用户创建成功");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return true;
    }


}
