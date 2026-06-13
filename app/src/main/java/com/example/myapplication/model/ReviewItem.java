package com.example.myapplication.model;

public class ReviewItem {
    // backend trả userName là object User, ta chỉ cần fullName
    private UserNameWrapper userName;
    private Integer ratingStar;
    private String comment;
    private String createdAt;
    private String imageUrl;

    public String getReviewerName() {
        if (userName != null && userName.getFullName() != null) return userName.getFullName();
        return "Khách hàng";
    }
    public Integer getRatingStar() { return ratingStar != null ? ratingStar : 0; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
    public String getImageUrl() { return imageUrl; }

    public static class UserNameWrapper {
        private String fullName;
        private String email;
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
    }
}
