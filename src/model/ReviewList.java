package model;

import java.util.ArrayList;
import java.util.List;

public class ReviewList {

    private double averageRating;
    private List<Review> reviewList;

    public ReviewList() {
        reviewList = new ArrayList<>();
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public List<Review> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    public Review get(int position) {
        return reviewList.get(position);
    }

    public void add(Review review) {
        reviewList.add(review);
    }

    public int size() {
        return this.reviewList.size();
    }
}
