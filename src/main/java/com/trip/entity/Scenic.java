package com.trip.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_scenic")
public class Scenic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 景区名称
     */
    private String name;

    /**
     * 景区类型的id
     */
    private Long typeId;

    /**
     * 景区图片，多个图片以','隔开
     */
    private String images;

    /**
     * 地区，例如西湖区
     */
    private String area;

    /**
     * 地址
     */
    private String address;

    /**
     * 经度
     */
    private Double x;

    /**
     * 维度
     */
    private Double y;

    /**
     * 均价，取整数
     */
    private Long avgPrice;

    /**
     * 销量
     */
    private Integer sold;

    /**
     * 评论数量
     */
    private Integer comments;

    /**
     * 评分，1~5分，乘10保存，避免小数
     */
    private Integer score;

    /**
     * 开放时间，例如 10:00-22:00
     */
    private String openHours;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private Double distance;

    @TableField(exist = false)
    private List<Blog> blogPreview;

    @TableField(exist = false)
    private String location;

    public static Scenic fromMap(Map<String, Object> data) {
        Scenic s = new Scenic();
        s.setId(Long.valueOf(data.get("id").toString()));
        s.setName((String) data.get("name"));
        s.setSold(Integer.valueOf(data.get("sold").toString()));
        s.setTypeId(Long.valueOf(data.get("type_id").toString()));
        s.setArea((String) data.get("area"));
        s.setAddress((String) data.get("address"));
        s.setImages((String) data.get("images"));
        s.setAvgPrice(Long.valueOf(data.get("avg_price").toString()));
        s.setOpenHours((String) data.get("open_hours"));
        s.setComments(Integer.valueOf(data.get("comments").toString()));
        s.setScore(Integer.valueOf(data.get("score").toString()));
        
        String lat = data.get("y").toString();
        String lon = data.get("x").toString();
        s.setLocation(lat + "," + lon);
        
        return s;
    }
}
