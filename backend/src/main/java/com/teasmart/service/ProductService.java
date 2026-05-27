package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teasmart.common.BusinessException;
import com.teasmart.dto.ProductDTO;
import com.teasmart.dto.SpecDTO;
import com.teasmart.entity.Category;
import com.teasmart.entity.Product;
import com.teasmart.entity.ProductSpec;
import com.teasmart.mapper.CategoryMapper;
import com.teasmart.mapper.ProductMapper;
import com.teasmart.mapper.ProductSpecMapper;
import com.teasmart.vo.ProductVO;
import com.teasmart.vo.SpecVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;
    private final CategoryMapper categoryMapper;

    public ProductService(ProductMapper productMapper,
                          ProductSpecMapper productSpecMapper,
                          CategoryMapper categoryMapper) {
        this.productMapper = productMapper;
        this.productSpecMapper = productSpecMapper;
        this.categoryMapper = categoryMapper;
    }

    public List<ProductVO> listByCategory(Long categoryId) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0)
                .orderByAsc(Product::getId);
        if (categoryId != null) {
            qw.eq(Product::getCategoryId, categoryId);
        }
        List<Product> products = productMapper.selectList(qw);
        return products.stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    public ProductVO getDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getDeleted() == 1) {
            throw BusinessException.notFound("商品不存在");
        }
        return toDetailVO(product);
    }

    public Page<Product> listAdmin(Long categoryId, String keyword, int page, int size) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .orderByDesc(Product::getId);
        if (categoryId != null) {
            qw.eq(Product::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Product::getName, keyword);
        }
        return productMapper.selectPage(new Page<>(page, size), qw);
    }

    @Transactional
    public ProductVO create(ProductDTO dto) {
        Category cat = categoryMapper.selectById(dto.getCategoryId());
        if (cat == null || cat.getDeleted() == 1) {
            throw BusinessException.badRequest("分类不存在");
        }

        Product product = new Product();
        product.setCategoryId(dto.getCategoryId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(dto.getImage());
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus());
        productMapper.insert(product);

        if (dto.getSpecs() != null) {
            for (SpecDTO spec : dto.getSpecs()) {
                ProductSpec ps = new ProductSpec();
                ps.setProductId(product.getId());
                ps.setSpecType(spec.getSpecType());
                ps.setSpecName(spec.getSpecName());
                ps.setPriceDiff(spec.getPriceDiff());
                productSpecMapper.insert(ps);
            }
        }

        return toDetailVO(product);
    }

    @Transactional
    public ProductVO update(Long id, ProductDTO dto) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getDeleted() == 1) {
            throw BusinessException.notFound("商品不存在");
        }

        product.setCategoryId(dto.getCategoryId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(dto.getImage());
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus());
        productMapper.updateById(product);

        // 删除旧规格，插入新规格
        productSpecMapper.delete(
                new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, id));
        if (dto.getSpecs() != null) {
            for (SpecDTO spec : dto.getSpecs()) {
                ProductSpec ps = new ProductSpec();
                ps.setProductId(id);
                ps.setSpecType(spec.getSpecType());
                ps.setSpecName(spec.getSpecName());
                ps.setPriceDiff(spec.getPriceDiff());
                productSpecMapper.insert(ps);
            }
        }

        return toDetailVO(product);
    }

    public void delete(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getDeleted() == 1) {
            throw BusinessException.notFound("商品不存在");
        }
        product.setDeleted(1);
        productMapper.updateById(product);
    }

    private ProductVO toSimpleVO(Product p) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getId());
        vo.setCategoryId(p.getCategoryId());
        vo.setName(p.getName());
        vo.setDescription(p.getDescription());
        vo.setImage(p.getImage());
        vo.setPrice(p.getPrice());
        vo.setStatus(p.getStatus());
        return vo;
    }

    private ProductVO toDetailVO(Product p) {
        ProductVO vo = toSimpleVO(p);

        Category cat = categoryMapper.selectById(p.getCategoryId());
        if (cat != null) {
            vo.setCategoryName(cat.getName());
        }

        List<ProductSpec> specs = productSpecMapper.selectList(
                new LambdaQueryWrapper<ProductSpec>()
                        .eq(ProductSpec::getProductId, p.getId()));
        Map<String, List<SpecVO>> grouped = specs.stream()
                .collect(Collectors.groupingBy(
                        ProductSpec::getSpecType,
                        Collectors.mapping(this::toSpecVO, Collectors.toList())));
        vo.setSpecs(grouped);

        return vo;
    }

    private SpecVO toSpecVO(ProductSpec ps) {
        SpecVO vo = new SpecVO();
        vo.setId(ps.getId());
        vo.setSpecType(ps.getSpecType());
        vo.setSpecName(ps.getSpecName());
        vo.setPriceDiff(ps.getPriceDiff());
        return vo;
    }
}
