package com.vm.admin.service.impl;

import com.google.common.collect.ImmutableMap;
import com.vm.admin.dao.mapper.*;
import com.vm.admin.dao.mapper.custom.*;
import com.vm.admin.dao.po.VmRoles;
import com.vm.admin.dao.po.VmRolesAuthsRealation;
import com.vm.admin.dao.qo.VmRolesQueryBean;
import com.vm.admin.service.dto.VmRolesDto;
import com.vm.admin.service.exception.VmRolesException;
import com.vm.admin.service.inf.VmAuthsService;
import com.vm.admin.service.inf.VmRolesService;
import com.vm.base.util.BaseService;
import com.vm.dao.util.BasePo;
import com.vm.dao.util.PageBean;
import com.vm.dao.util.QuickSelectOne;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by ZhangKe on 2018/3/26.
 */
@Service
public class VmRolesServiceImpl extends BaseService implements VmRolesService {
    @Autowired
    VmAdminsMapper vmAdminsMapper;
    @Autowired
    CustomVmAdminsMapper customVmAdminsMapper;
    @Autowired
    VmAdminsLoginLogsMapper vmAdminsLoginLogsMapper;
    @Autowired
    VmMenusMapper vmAuthMenusMapper;
    @Autowired
    CustomVmRolesMenusRealationMapper customVmRolesMenusRealationMapper;
    @Autowired
    VmAuthsMapper vmAuthsMapper;
    @Autowired
    CustomVmRolesAuthsRealationMapper customVmRolesAuthsRealationMapper;
    @Autowired
    VmRolesAuthsRealationMapper vmRolesAuthsRealationMapper;
    @Autowired
    VmAdminsRolesRealationMapper vmAdminsRolesRealationMapper;
    @Autowired
    CustomVmRolesMapper customVmRolesMapper;
    @Autowired
    CustomVmMenusMapper customVmAuthMenusMapper;
    @Autowired
    CustomVmAuthsMapper customVmAuthsMapper;
    @Autowired
    VmRolesMapper vmRolesMapper;
    @Autowired
    CustomVmAdminsRolesRealationMapper customVmAdminsRolesRealationMapper;


    @Override
    public List<VmRolesDto> getRoles(PageBean page, VmRolesQueryBean query) {
        List<VmRoles> vmRoles = customVmRolesMapper.getRoles(page, query);
        return makeRolesDtos(vmRoles);
    }

    @Override
    public Long getRolesTotal(PageBean page, VmRolesQueryBean query) {
        return customVmRolesMapper.getRolesTotal(page, query);
    }

    @Override
    public VmRolesDto addRole(VmRolesDto vmRolesDto) {
        VmRoles vmRoles = vmRolesMapper.selectOneBy(ImmutableMap.of(
                "roleName", vmRolesDto.getRoleName(),
                "isDeleted", BasePo.IsDeleted.NO.getCode()
        ));

        if (!isNullObject(vmRoles)) {
            throw new VmRolesException("addRole role name is exits !! vmRolesDto is :" + vmRolesDto,
                    VmRolesException.ErrorCode.ROLE_NAME_IS_EXITS.getCode(),
                    VmRolesException.ErrorCode.ROLE_NAME_IS_EXITS.getMsg());
        }


        vmRoles = makeAddRole(vmRolesDto);

        if (1 != vmRolesMapper.insert(vmRoles)) {
            throw new VmRolesException("addRole vmRolesMapper#insert is fail !! vmRolesDto is :" + vmRolesDto);
        }

        //get new obj
        vmRoles = this.getRoleById(vmRoles.getId(), BasePo.IsDeleted.NO);

        return makeRolesDto(vmRoles);
    }

    @Override
    @Transactional
    public VmRolesDto editRole(VmRolesDto vmRolesDto) {
        Long roleId = vmRolesDto.getId();

        VmRoles vmRoles = this.getRoleById(roleId, BasePo.IsDeleted.NO);


        if (!vmRoles.getRoleName().equals(vmRolesDto.getRoleName())) {//if change username
            vmRoles = vmRolesMapper.selectOneBy(ImmutableMap.of(
                    "roleName", vmRolesDto.getRoleName(),
                    "isDeleted", BasePo.IsDeleted.NO.getCode()
            ));
            if (!isNullObject(vmRoles)) {
                throw new VmRolesException("editRole role name is exits !! vmRolesDto is :" + vmRolesDto,
                        VmRolesException.ErrorCode.ROLE_NAME_IS_EXITS.getCode(),
                        VmRolesException.ErrorCode.ROLE_NAME_IS_EXITS.getMsg());
            }
        }

        //delete auth realation

        List<Long> realationIds = vmRolesAuthsRealationMapper.selectIdList(ImmutableMap.of(
                "isDeleted", BasePo.IsDeleted.NO.getCode(),
                "roleId", roleId
        ));

        if (!isEmptyList(realationIds)) {
            if (0 > vmRolesAuthsRealationMapper.updateInIds(realationIds, ImmutableMap.of(
                    "isDeleted", BasePo.IsDeleted.YES.getCode()
            ))) {
                throw new VmRolesException("editRole vmRolesAuthsRealationMapper#updateInIds is fail ! vmRolesDto is : " + vmRolesDto);
            }
        }
        //insert new auth,authIds
        String authIdsStr = vmRolesDto.getAuthIds();
        if (!isEmptyString(authIdsStr)) {
            List<Long> authIds = parseStringArray2Long(authIdsStr);
            List<VmRolesAuthsRealation> newRealations = makeVmRolesAuthsRealations(roleId, authIds);

            if (newRealations.size() != vmRolesAuthsRealationMapper.batchInsert(newRealations)) {
                throw new VmRolesException("editRole vmRolesAuthsRealationMapper#batchInsert is fail ! vmRolesDto is : " + vmRolesDto);
            }
        }


        //update role
        vmRoles = makeEditRole(vmRolesDto);

        if (1 != vmRolesMapper.update(roleId, vmRoles)) {
            throw new VmRolesException("editRole vmRolesMapper#update is fail !! vmRolesDto is :" + vmRolesDto);
        }

        //get new obj
        vmRoles = this.getRoleById(roleId, BasePo.IsDeleted.NO);
        return makeRolesDto(vmRoles);
    }

    private List<VmRolesAuthsRealation> makeVmRolesAuthsRealations(Long roleId, List<Long> authIds) {
        return authIds.stream().parallel().map(authId -> {
            return makeVmRolesAuthsRealation(roleId, authId);
        }).collect(toList());
    }

    private VmRolesAuthsRealation makeVmRolesAuthsRealation(Long roleId, Long authId) {
        Integer now = now();
        VmRolesAuthsRealation vmRolesAuthsRealation = new VmRolesAuthsRealation();
        vmRolesAuthsRealation.setAuthId(authId);
        vmRolesAuthsRealation.setRoleId(roleId);
        vmRolesAuthsRealation.setIsDeleted(BasePo.IsDeleted.NO.getCode());
        vmRolesAuthsRealation.setStatus(BasePo.Status.NORMAL.getCode());
        vmRolesAuthsRealation.setCreateTime(now);
        vmRolesAuthsRealation.setUpdateTime(now);
        return vmRolesAuthsRealation;
    }


    private VmRoles makeEditRole(VmRolesDto vmRolesDto) {
        Integer now = now();
        VmRoles vmRoles = new VmRoles();
        vmRoles.setDescription(vmRolesDto.getDescription());
        vmRoles.setRoleName(vmRolesDto.getRoleName());
        vmRoles.setStatus(vmRolesDto.getStatus());
        vmRoles.setUpdateTime(now);
        return vmRoles;
    }

    private VmRoles makeAddRole(VmRolesDto vmRolesDto) {
        Integer now = now();
        VmRoles vmRoles = new VmRoles();
        vmRoles.setDescription(vmRolesDto.getDescription());
        vmRoles.setImmutable(BasePo.Immutable.NO.getCode());
        vmRoles.setRoleName(vmRolesDto.getRoleName());
        vmRoles.setStatus(vmRolesDto.getStatus());
        vmRoles.setIsDeleted(BasePo.IsDeleted.NO.getCode());
        vmRoles.setCreateTime(now);
        vmRoles.setUpdateTime(now);
        return vmRoles;
    }

    private List<VmRolesDto> makeRolesDtos(List<VmRoles> vmRoles) {
        return vmRoles.stream().parallel().map(r -> {
            return makeRolesDto(r);
        }).collect(toList());
    }

    private VmRolesDto makeRolesDto(VmRoles vmRoles) {
        VmRolesDto vmRolesDto = new VmRolesDto();
        vmRolesDto.setId(vmRoles.getId());
        vmRolesDto.setDescription(vmRoles.getDescription());
        vmRolesDto.setImmutable(vmRoles.getImmutable());
        vmRolesDto.setRoleName(vmRoles.getRoleName());
        vmRolesDto.setCreateTime(vmRoles.getCreateTime());
        vmRolesDto.setUpdateTime(vmRoles.getUpdateTime());
        vmRolesDto.setStatus(vmRoles.getStatus());
        return vmRolesDto;
    }

    private VmRoles getRoleById(Long id, BasePo.Status status, BasePo.IsDeleted isDeleted) {

        return QuickSelectOne.getObjectById(vmRolesMapper, id, status, isDeleted);
    }

    private VmRoles getRoleById(Long id, BasePo.IsDeleted isDeleted) {

        return QuickSelectOne.getObjectById(vmRolesMapper, id, isDeleted);
    }
}
