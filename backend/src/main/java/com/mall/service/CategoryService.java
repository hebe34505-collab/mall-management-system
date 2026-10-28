package com.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;
import com.mall.domain.AccessPolicy;
import com.mall.dto.CategorySaveRequest;
import com.mall.entity.Category;
import com.mall.entity.Product;
import com.mall.mapper.CategoryMapper;
import com.mall.mapper.ProductMapper;
import com.mall.security.LoginUser;
import com.mall.security.SecurityUtils;
import com.mall.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final OperationLogService operationLogService;

    public List<CategoryVO> listPublic() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId));
        return toVo(categories);
    }

    public List<CategoryVO> listAdmin() {
        assertAdmin();
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId));
        return toVo(categories);
    }

    public CategoryVO detail(Long id, boolean admin) {
        Category category = categoryMapper.selectById(id);
        if (category == null || (!admin && (category.getStatus() == null || category.getStatus() != 1))) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        if (admin) {
            assertAdmin();
        }
        return CategoryVO.from(category, countProducts(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryVO create(CategorySaveRequest request) {
        LoginUser admin = assertAdmin();
        ensureNameUnique(request.getName(), null);
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(normalizeStatus(request.getStatus()));
        categoryMapper.insert(category);
        operationLogService.record(admin.getId(), admin.getUsername(), "CATEGORY", "CREATE", "新增分类 " + category.getName());
        return CategoryVO.from(category, 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryVO update(Long id, CategorySaveRequest request) {
        LoginUser admin = assertAdmin();
        Category category = require(id);
        ensureNameUnique(request.getName(), id);
        category.setName(request.getName().trim());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(normalizeStatus(request.getStatus()));
        categoryMapper.updateById(category);
        operationLogService.record(admin.getId(), admin.getUsername(), "CATEGORY", "UPDATE", "编辑分类 " + category.getName());
        return CategoryVO.from(category, countProducts(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        LoginUser admin = assertAdmin();
        Category category = require(id);
        if (countProducts(id) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS, "该分类下仍有商品，不能删除");
        }
        categoryMapper.deleteById(id);
        operationLogService.record(admin.getId(), admin.getUsername(), "CATEGORY", "DELETE", "删除分类 " + category.getName());
    }

    private List<CategoryVO> toVo(List<Category> categories) {
        return categories.stream().map(category -> CategoryVO.from(category, countProducts(category.getId()))).toList();
    }

    private long countProducts(Long categoryId) {
        Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, categoryId));
        return count == null ? 0 : count;
    }

    private void ensureNameUnique(String name, Long currentId) {
        Category existing = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, name.trim()).last("LIMIT 1"));
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException(ErrorCode.CONFLICT, "分类名称已存在");
        }
    }

    private Category require(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    private int normalizeStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "分类状态只能是 0 或 1");
        }
        return status;
    }

    private LoginUser assertAdmin() {
        LoginUser user = SecurityUtils.current();
        if (!AccessPolicy.isAdmin(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限访问该资源");
        }
        return user;
    }
}
