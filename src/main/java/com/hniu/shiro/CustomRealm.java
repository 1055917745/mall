package com.hniu.shiro;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.hniu.pojo.ActiveUser;
import com.hniu.pojo.SysPermission;
import com.hniu.pojo.SysUser;
import com.hniu.service.SysService;

/**
 * @author 熊俊
 *自定义realm
 * @date 2018年5月12日
 *
 */
public class CustomRealm extends AuthorizingRealm{
	
	@Autowired
	SysService sysService;

	//设置realm的名称
	@Override
	public void setName(String name) {
		
		super.setName("customRealm");
	}


	//用于授权
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		//从principals获取主身份信息
		//将getPrimaryPrincipal方法返回值转为真实身份类型（在下边的doGetAuthenticationInfo认证通过填充到SimpleAuthenticationInfo中身份类型），
		ActiveUser activeUser =  (ActiveUser)principals.getPrimaryPrincipal();
		
		//根据身份信息获取权限信息
		//链接数据库.....
		//从数据库获取到权限数据
		
		List<SysPermission> permissionList = null;
		try {
			permissionList = sysService.findPermissionListByUserId(activeUser.getUserid());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> permissions = new ArrayList<String>();
		if(permissionList != null) {
			for(SysPermission sysPermission:permissionList) {
				//将数据库中的权限标签放入集合
			permissions.add(sysPermission.getPercode());
			}
		}
		
		/*List<String> permissions = new ArrayList<String>();
		permissions.add("user:create");//用户创建
		permissions.add("items:add");//商品的添加权限
		permissions.add("items:query");//商品查询权限
		permissions.add("items:edit");//商品修改权限
		//.......
		*/
		//查到权限数据，返回授权信息(要包括permissions)
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		//将上边查询到的授权信息填充到simpleAuthorizationInfo对象中
		simpleAuthorizationInfo.addStringPermissions(permissions);
		return simpleAuthorizationInfo;
	}

	
	//用于认证
	//realm的认证方法，从数据库查询用户信息
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
	
		//token是用户输入的用户名和密码
		//第一步：从token中取出用户的身份信息
		String userCode = (String) token.getPrincipal();
		
		//第二步：根据用户输入的userCode从数据库中查询
		SysUser sysUser = null;
		try {
			sysUser = sysService.findSysUserByUserCode(userCode);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//如果查询不到返回null
		//数据库中用账号是zhangsan
		if(sysUser==null) {
			return null;
		}
		
		
		//从数据库查询到的密码
		String password = sysUser.getPassword();
		
		//盐
		String salt = sysUser.getSalt();
		
		//如果查询不到返回null
		
		//如果查询到返回认证信息AuthenticationInfo
		//activeUser就是用户的身份信息
		ActiveUser  activeUser = new ActiveUser();
		activeUser.setUserid(sysUser.getId());
		activeUser.setUsercode(sysUser.getUsercode());
		activeUser.setUsername(sysUser.getUsername());
		//。。。。。。。
		
		//根据 用户id取出菜单
		
		List<SysPermission> menus = null;
		try {
			//通过service取出菜单
			menus = sysService.findMenuListByUserId(sysUser.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//将用户菜单设置到activeUser
		activeUser.setMenus(menus);
		//将activeUser设置simpleAuthenticationInfo
		//SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(activeUser, password,ByteSource.Util.bytes(salt), this.getName());
		SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(
				activeUser, password,ByteSource.Util.bytes(salt), this.getName());

		return simpleAuthenticationInfo;
	}
	
	//清除缓存
		public void clearCached() {
			PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
			super.clearCache(principals);
		}

}
