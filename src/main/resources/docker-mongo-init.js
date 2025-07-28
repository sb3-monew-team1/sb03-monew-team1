// MongoDB admin 사용자 생성
db.createUser({
  user: "mongo_root_user",
  pwd: "mongo_root_password",
  roles: [
    { role: "root", db: "admin" },
    { role: "readWrite", db: "monew_mongo" }
  ]
});

// 일반 사용자 생성
db.createUser({
  user: "mongo_monew_user",
  pwd: "mongo1234",
  roles: [
    { role: "readWrite", db: "monew_mongo" }
  ]
});