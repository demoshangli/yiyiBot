package com.bot.yiyi.Pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.bot.yiyi.config.NumericBooleanDeserializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class MessageReceiver {
    private String type;
    private String content;
    private Source source;

    // 使用FastJSON注解实现字符串到boolean的转换
    @JSONField(deserializeUsing = NumericBooleanDeserializer.class)
    private boolean isMentioned;

    @JSONField(deserializeUsing = NumericBooleanDeserializer.class)
    private boolean isMsgFromSelf;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Source getSource() { return source; }
    public void setSource(String source) {
        this.source = JSON.parseObject(source.replace("\\\\\\\"", "\""), Source.class);
    }

    public boolean isMentioned() { return isMentioned; }
    public void setMentioned(boolean mentioned) { isMentioned = mentioned; }

    public boolean isMsgFromSelf() { return isMsgFromSelf; }
    public void setMsgFromSelf(boolean msgFromSelf) { isMsgFromSelf = msgFromSelf; }

    // 嵌套结构定义
    public static class Source {
        private Room room;
        private Contact to;
        private Contact from;

        public Room getRoom() {
            return room;
        }

        public void setRoom(Room room) {
            this.room = room;
        }

        public Contact getTo() {
            return to;
        }

        public void setTo(Contact to) {
            this.to = to;
        }

        public Contact getFrom() {
            return from;
        }

        public void setFrom(Contact from) {
            this.from = from;
        }
    }

    public static class Contact {
        private String id;
        private Payload payload;
        @JSONField(name = "_events", serialize = false, deserialize = false)
        private Object _events;

        @JSONField(name = "_eventsCount", serialize = false, deserialize = false)
        private int _eventsCount;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Object get_events() {
            return _events;
        }

        public void set_events(Object _events) {
            this._events = _events;
        }

        public int get_eventsCount() {
            return _eventsCount;
        }

        public void set_eventsCount(int _eventsCount) {
            this._eventsCount = _eventsCount;
        }

        public Payload getPayload() { return payload; }
        public void setPayload(Payload payload) { this.payload = payload; }
    }

    public static class Payload {
        // 公共字段
        private String alias;
        private String avatar;
        private boolean friend;
        private int gender;
        private String id;
        private String name;
        private List<String> phone;
        private boolean star;
        private int type;
        private String signature;

        // from特有字段
        private String city;
        private String province;

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public boolean isFriend() {
            return friend;
        }

        public void setFriend(boolean friend) {
            this.friend = friend;
        }

        public int getGender() {
            return gender;
        }

        public void setGender(int gender) {
            this.gender = gender;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getPhone() {
            return phone;
        }

        public void setPhone(List<String> phone) {
            this.phone = phone;
        }

        public boolean isStar() {
            return star;
        }

        public void setStar(boolean star) {
            this.star = star;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }
    }

    public static class Room {
        private String id;
        private String topic;
        private GroupPayload payload;

        @JSONField(serialize = false)
        private Object _events;

        @JSONField(serialize = false)
        private int _eventsCount;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public GroupPayload getPayload() {
            return payload;
        }

        public void setPayload(GroupPayload payload) {
            this.payload = payload;
        }

        public Object get_events() {
            return _events;
        }

        public void set_events(Object _events) {
            this._events = _events;
        }

        public int get_eventsCount() {
            return _eventsCount;
        }

        public void set_eventsCount(int _eventsCount) {
            this._eventsCount = _eventsCount;
        }
    }

    // 群组专用payload
    public static class GroupPayload {
        private String id;
        private List<String> adminIdList;
        private String avatar;
        private List<GroupMember> memberList;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getAdminIdList() {
            return adminIdList;
        }

        public void setAdminIdList(List<String> adminIdList) {
            this.adminIdList = adminIdList;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public List<GroupMember> getMemberList() {
            return memberList;
        }

        public void setMemberList(List<GroupMember> memberList) {
            this.memberList = memberList;
        }
    }

    // 群成员结构
    public static class GroupMember {
        @JSONField(name = "id")
        private String memberId;

        private String avatar;
        private String name;

        @JSONField(name = "alias")
        private String personalRemark; // 个人备注名（非群备注）

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPersonalRemark() {
            return personalRemark;
        }

        public void setPersonalRemark(String personalRemark) {
            this.personalRemark = personalRemark;
        }

        public String getMemberId() { return memberId; }
        public void setMemberId(String memberId) { this.memberId = memberId; }
    }
}