# akka-http-study
http://yangcongchufang.com/kick-akka-http.html

生成SSH密钥 并 添加公共密钥到GitHub上的帐户：
//设置GitHub的user name和email：git config --global user.name "Git账号" git config --global user.email "Git邮箱"
生成一个新的SSH密钥：ssh-keygen -t rsa -C "your_email@example.com"

本地项目首次发送到GitHub：
git init -- 新建一个本地仓库
git add README.md -- 将README.md文件加入到仓库中
git commit -m "first commit" -- 将文件commit到本地仓库
git remote add origin https://github.com/XuDaojie/Lee.git -- 添加远程仓库，origin只是一个远程仓库的别名，可以随意取
git push -u origin master -- 将本地仓库push远程仓库，并将origin设为默认远程仓库