package com.github.rmpestano.tdc.cars.infra.security;

import com.github.adminfaces.template.exception.AccessDeniedException;
import com.github.adminfaces.template.session.AdminSession;

import org.apache.deltaspike.security.api.authorization.Secures;
import org.omnifaces.util.Faces;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;
import javax.interceptor.InvocationContext;

import java.io.IOException;
import java.io.Serializable;

import static com.github.adminfaces.persistence.util.Messages.addDetailMessage;


/**
 * Created by rmpestano on 12/20/14.
 *
 * This is just a login example.
 *
 * AdminSession uses isLoggedIn to determine if user must be redirect to login page or not.
 * By default AdminSession isLoggedIn always resolves to true so it will not try to redirect user.
 *
 * If you already have your authorization mechanism which controls when user must be redirect to initial page or logon
 * you can skip this class.
 */
@Named
@SessionScoped
@Specializes
public class LogonMB extends AdminSession implements Serializable {

    private String currentUser;
    private String email;
    private String password;
    private boolean remember;


    public void login() throws IOException {
        currentUser = email;
        addDetailMessage("Logged in successfully as <b>" + email + "</b>");
        Faces.getExternalContext().getFlash().setKeepMessages(true);
        Faces.redirect("index.xhtml");
    }
    
    public void login(String user)  {
    	setCurrentUser(user);
    }
    
    @Secures
    @Admin
    public boolean doAdminCheck(InvocationContext invocationContext, BeanManager manager) throws Exception {
        boolean allowed = currentUser != null && currentUser.equals("admin");
        if(!allowed){
            throw new AccessDeniedException("Access denied");
        }
        return allowed;
    }

    @Secures
    @Guest
    public boolean doGuestCheck(InvocationContext invocationContext, BeanManager manager) throws Exception {
        boolean allowed = currentUser != null && currentUser.equals("guest") || doAdminCheck(null, null);
        if(!allowed){
            throw new AccessDeniedException("Access denied");
        }
        return allowed;
    }

    @Override
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }
}
