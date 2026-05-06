# LTNC
cai java:
sudo apt install openjdk-17-jdk -y
java -version


tao file output
mkdir -p out
find src -name "*.java" | xargs javac -d out -encoding UTF-8

chay :
java -cp out com.phenikaa.library.MainApp

python3 -m http.server 8080
http://127.0.0.1:8080/lib.html#
