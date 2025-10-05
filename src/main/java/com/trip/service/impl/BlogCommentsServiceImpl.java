package com.trip.service.impl;

import com.trip.entity.BlogComments;
import com.trip.mapper.BlogCommentsMapper;
import com.trip.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
