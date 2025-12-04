// ===== FIREBASE FUNCTIONS V2 – CHUẨN KHÔNG LỖI =====
const { onDocumentCreated, onDocumentDeleted } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

// Set vùng gần Việt Nam
setGlobalOptions({ region: "asia-southeast1", maxInstances: 3 });



admin.initializeApp();
const db = admin.firestore();

// ========== 1. Khi tạo bài viết ==========
exports.onPostCreated = onDocumentCreated("posts/{postId}", async (event) => {
  const post = event.data.data();
  const userRef = db.collection("users").doc(post.userId);

  return userRef.update({
    postCount: admin.firestore.FieldValue.increment(1),
  });
});

// ========== 2. Khi xóa bài viết ==========
exports.onPostDeleted = onDocumentDeleted("posts/{postId}", async (event) => {
  const post = event.data.data();
  const postId = event.params.postId;

  const batch = db.batch();

  // Giảm số bài của user
  const userRef = db.collection("users").doc(post.userId);
  batch.update(userRef, {
    postCount: admin.firestore.FieldValue.increment(-1),
  });

  // Xóa likes
  const likesSnap = await db.collection("posts").doc(postId).collection("likes").get();
  likesSnap.forEach((doc) => batch.delete(doc.ref));

  // Xóa comments
  const commentsSnap = await db.collection("posts").doc(postId).collection("comments").get();
  commentsSnap.forEach((doc) => batch.delete(doc.ref));

  // Xóa images
  const imagesSnap = await db.collection("posts").doc(postId).collection("images").get();
  imagesSnap.forEach((doc) => batch.delete(doc.ref));

  return batch.commit();
});

// ========== 3. Khi tạo LIKE ==========
exports.onLikeCreated = onDocumentCreated("posts/{postId}/likes/{userId}", async (event) => {
  const postRef = db.collection("posts").doc(event.params.postId);
  const postSnap = await postRef.get();
  const post = postSnap.data();

  const batch = db.batch();

  // Tăng likeCount
  batch.update(postRef, {
    likeCount: admin.firestore.FieldValue.increment(1),
  });

  // Tăng tổng like nhận được của tác giả
  batch.update(db.collection("users").doc(post.userId), {
    totalLikesReceived: admin.firestore.FieldValue.increment(1),
  });

  return batch.commit();
});

// ========== 4. Khi un-like ==========
exports.onLikeDeleted = onDocumentDeleted("posts/{postId}/likes/{userId}", async (event) => {
  const postRef = db.collection("posts").doc(event.params.postId);
  const postSnap = await postRef.get();
  const post = postSnap.data();

  const batch = db.batch();

  batch.update(postRef, {
    likeCount: admin.firestore.FieldValue.increment(-1),
  });

  batch.update(db.collection("users").doc(post.userId), {
    totalLikesReceived: admin.firestore.FieldValue.increment(-1),
  });

  return batch.commit();
});

// ========== 5. Khi tạo comment ==========
exports.onCommentCreated = onDocumentCreated("posts/{postId}/comments/{commentId}", (event) => {
  return db.collection("posts").doc(event.params.postId).update({
    commentCount: admin.firestore.FieldValue.increment(1),
  });
});

// ========== 6. Khi xóa comment ==========
exports.onCommentDeleted = onDocumentDeleted("posts/{postId}/comments/{commentId}", (event) => {
  return db.collection("posts").doc(event.params.postId).update({
    commentCount: admin.firestore.FieldValue.increment(-1),
  });
});
