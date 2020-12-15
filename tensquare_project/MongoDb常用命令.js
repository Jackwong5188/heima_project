//创建数据库
use commentdb;

//插入文档的同时创建集合  db.集合名称.insert(数据);
db.comment.insert({content:"十次方课程",userid:"1011"})
db.comment.insert({title:"黑马",userid:"1011"})
db.comment.insert({_id:"1",content:"到底为啥出错",userid:"1012",thumbup:2020});
db.comment.insert({_id:"2",content:"加班到半夜",userid:"1013",thumbup:1023});
db.comment.insert({_id:"3",content:"手机流量超了咋办",userid:"1013",thumbup:111});
db.comment.insert({_id:"4",content:"坚持就是胜利",userid:"1014",thumbup:1223});
db.comment.insert({_id:"5",content:"手机没电了啊",userid:"1014",thumbup:923});
db.comment.insert({_id:"6",content:"这个手机好",userid:"1014",thumbup:123});

//1.查询 db.集合名称.find(条件)
//1.1查询 评论集合中userid为1011的数据
db.comment.find({userid:"1011"})
//1.2 模糊查询   使用正则表达式   /模糊查询的内容/
db.comment.find({content:/手机/})
//1.3 查询评论点赞数大于1000的记录 
db.comment.find({thumbup:{$gt:1000}})
//1.4 包含与不包含  查询userid包含在["1013","1014"]
db.comment.find({userid:{$in:["1013","1014"]}})
//1.5 多条件查询  $and:[ {条件},{条件},{条件} ]
//1.5.1 查询用户id为1013且点赞数大于等于1000的文档
db.comment.find({$and:[{userid:"1013"},{thumbup:{$gte:1000}}]})
//1.6 多条件查询  $or:[ {条件},{条件},{条件} ]
//1.6.1 查询用户id为1013或者id为1014的文档
db.comment.find({$or:[{userid:"1013"},{userid:"1014"}]})

//2 修改 db.集合名称.update(条件,修改后的数据)
//2.1 修改_id为1的记录，点赞数为1000
db.comment.update({_id:"1"},{thumbup:1000})
//2.2 使用$set操作符修改某个属性, db.集合名称.update(条件,{$set:{字段:值}})
db.comment.update({_id:"2"},{$set:{thumbup:1000}})
//2.3 _id为2的点赞数+1  $inc
db.comment.update({_id:"2"},{$inc:{thumbup:1}})
//2.4 _id为2的点赞数-1  $inc
db.comment.update({_id:"2"},{$inc:{thumbup:-1}})

//3 删除 db.集合名称.remove(条件)
db.comment.remove({_id:"1"})

//4 统计条数  db.comment.count(条件)
//4.1 统计所有
db.comment.count()
//4.2 统计用户id为1013
db.comment.count({userid:"1013"})







