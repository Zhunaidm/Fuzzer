export ASM_HOME="/home/zhunaid/asm-6.0/lib"
rm -f *.class
rm -f *.bk
javac -cp .:$ASM_HOME/asm-6.0.jar:$ASM_HOME/asm-commons-6.0.jar Instrumenter.java
javac $1.java
java -cp .:$ASM_HOME/asm-6.0.jar:$ASM_HOME/asm-commons-6.0.jar Instrumenter $1.class $1.class



