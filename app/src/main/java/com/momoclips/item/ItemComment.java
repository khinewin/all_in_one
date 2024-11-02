package com.momoclips.item;

public class ItemComment {
	
	private String CommentId;
	private String CommentName;
	private String CommentMsg;
	private String CommentImage;
	private String CommentUserId;
	private String CommentPostId;
	


	public String getCommentName() {
		return CommentName;
	}
 	public void setCommentName(String CommentName) {
		this.CommentName = CommentName;
	}
	
	public String getCommentMsg() { return CommentMsg; }
	public void setCommentMsg(String CommentMsg) { this.CommentMsg=CommentMsg; }

	public String getCommentImage() { return CommentImage; }
	public void setCommentImage(String CommentImage) { this.CommentImage=CommentImage; }


	public String getCommentUserId() {
		return CommentUserId;
	}

	public void setCommentUserId(String commentUserId) {
		CommentUserId = commentUserId;
	}

	public String getCommentPostId() {
		return CommentPostId;
	}

	public void setCommentPostId(String commentPostId) {
		CommentPostId = commentPostId;
	}

	public String getCommentId() {
		return CommentId;
	}

	public void setCommentId(String commentId) {
		CommentId = commentId;
	}
}
