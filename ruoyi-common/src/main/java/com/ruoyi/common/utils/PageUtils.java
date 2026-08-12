package com.ruoyi.common.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.sql.SqlUtil;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 * 
 * @author ruoyi
 */
public class PageUtils extends PageHelper {
    /**
     * 设置请求分页数据
     */
    public static void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage() {
        PageHelper.clearPage();
    }

    /**
     * 将 PageHelper 查询结果映射成新的 VO 列表时，保留原始分页元信息。
     *
     * @param source 原始查询结果，可能是 PageHelper 返回的 Page
     * @param target 转换后的目标列表
     * @param <S> 原始元素类型
     * @param <T> 目标元素类型
     * @return 保留分页元信息后的列表
     */
    public static <S, T> List<T> toPagedList(List<S> source, List<T> target) {
        if (!(source instanceof Page<?> sourcePage)) {
            return target;
        }
        Page<T> resultPage = new Page<>(sourcePage.getPageNum(), sourcePage.getPageSize());
        resultPage.setTotal(sourcePage.getTotal());
        resultPage.addAll(target);
        return resultPage;
    }

    /**
     * 将分页查询结果映射成新的列表，并自动保留 PageHelper 的分页元信息。
     *
     * @param source 原始查询结果
     * @param mapper 元素转换函数
     * @param <S> 原始元素类型
     * @param <T> 目标元素类型
     * @return 转换后的列表；若 source 为 Page，则返回保留分页信息的 Page
     */
    public static <S, T> List<T> mapPage(List<S> source, Function<? super S, ? extends T> mapper) {
        List<T> target = source.stream().map(mapper).collect(Collectors.toList());
        return toPagedList(source, target);
    }
}
