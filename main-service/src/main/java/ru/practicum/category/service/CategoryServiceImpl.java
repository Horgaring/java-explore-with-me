package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.RequestCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventService eventService;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        try {
            Category category = categoryMapper.toCategory(newCategoryDto);
            Category savedCategory = categoryRepository.save(category);
            return categoryMapper.toCategoryDto(savedCategory);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Category name must be unique");
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, RequestCategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (categoryDto.getName() != null &&
                !categoryDto.getName().equals(existingCategory.getName()) &&
                categoryRepository.existsByNameAndIdNot(categoryDto.getName(), catId)) {
            throw new ConflictException("Category name must be unique");
        }

        if (categoryDto.getName() != null) {
            existingCategory.setName(categoryDto.getName());
        }

        try {
            return categoryMapper.toCategoryDto(existingCategory);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Category name must be unique");
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (!eventService.findByCategoryId(catId).isEmpty()) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        return categories.stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        return categoryMapper.toCategoryDto(category);
    }
}