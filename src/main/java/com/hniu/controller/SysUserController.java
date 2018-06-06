package com.hniu.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hniu.exception.CustomException;
import com.hniu.pojo.SysPermission;
import com.hniu.pojo.SysUser;
import com.hniu.pojo.vo.SysUserVo;
import com.hniu.service.SysService;
import com.hniu.service.SysUserService;
import com.hniu.util.Page;
import com.hniu.util.State;

@Controller
@RequestMapping(value="/user")
public class SysUserController {
	@Autowired
	SysUserService userService;
	@Autowired
	SysService sysService;
	
	//查询所有用户
	@GetMapping(value="/selectAll")
	public String selectAll(Model model,Page page,Integer id) throws Exception {
		PageHelper.offsetPage(page.getStart(), page.getCount());
		List<SysUser> list = userService.selectAll();
		int total = (int) new PageInfo<>(list).getTotal();//通过PageInfo获取总数
		try {
			List<SysPermission> permissions = sysService.findPermissionListByUserId(id);
			model.addAttribute("permission",permissions);
		} catch (Exception e) {
			throw new CustomException("查询权限url出错");
		}
		page.setTotal(total);
		model.addAttribute("list",list);
		model.addAttribute("page",page);
		return "admin/editUserList";
	}
	
	 //查询单个用户,用户账号查询
	@GetMapping(value="/selectUser")
	public String selectUser(Model model,String usercode) {
		SysUser user = userService.selectUser(usercode);
		List<SysUser> list = new ArrayList<>();
		list.add(user);
		model.addAttribute("list",list);
		return "admin/editUserList";
	}
	
	//修改单个用户
	@RequestMapping(value="/updateUser")
	@ResponseBody
	public State updateUser(SysUser user) {
		State state = new State();
		int i = userService.updateUser(user);
		if(i==1) {
			state.setIsSuccess(true);
			state.setMessage("数据更新成功");
			return state;
		}else if(i == 2){
			state.setIsSuccess(false);
			state.setMessage("此用户不存在");
			return state;
		}else {
			state.setIsSuccess(false);
			state.setMessage("数据更新失败");
			return state;
		}
	}
	//删除单个用户
	@RequestMapping(value="/deleteUser")
	@ResponseBody
	public State deleteUser(Integer id) {
		State state = new State();
		int i = userService.deleteUser(id);
		if(i==1) {
			state.setIsSuccess(true);
			state.setMessage("用户删除成功成功");
			return state;
		}else if(i == 2){
			state.setIsSuccess(false);
			state.setMessage("此用户不存在");
			return state;
		}else {
			state.setIsSuccess(false);
			state.setMessage("用户删除失败");
			return state;
		}
	}
	//批量删除
	@RequestMapping(value="/deleteBatch")
	@ResponseBody
	public State deleteBatch(@RequestParam("id") Integer[] id) {
		State state = new State();
		int i = userService.deleteBatch(id);
		if(i == 1) {
			state.setIsSuccess(true);
			state.setMessage("用户批量删除成功");
			return state;
		}else {
			state.setIsSuccess(false);
			state.setMessage("用户批量删除失败");
			return state;
		}
		
	}
	
	
	
	
}