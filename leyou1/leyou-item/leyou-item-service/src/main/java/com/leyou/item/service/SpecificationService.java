package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据种类id查询起规格参数组信息
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsByCid(Long cid) {

        SpecGroup record = new SpecGroup();
        record.setCid(cid);

        List<SpecGroup> specGroups = this.specGroupMapper.select(record);

        return specGroups;
    }

    /**规格参数组id查询其规则参数信息
     * 根据
     *
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {

        SpecParam record = new SpecParam();

        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);

        return this.specParamMapper.select(record);

    }

    /**
     * 根据cid  查询到规格参数组及对应的规格参数
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupWithParamByCid(Long cid) {

        List<SpecGroup> groups = this.queryGroupsByCid(cid);

        groups.forEach(group->{
            List<SpecParam> params = this.queryParams(group.getId(), null, null, null);
            group.setParams(params);
        });

        return groups;
    }
}
