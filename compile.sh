export ASM_HOME="/home/zhunaid/Downloads/asm-6.0/lib"
rm -f *.class
rm -f *.bk
javac -cp $ASM_HOME/asm-6.0.jar Copy.java
javac DB_test.java
mv DB_test.class DB_test.class.bk
java -cp .:$ASM_HOME/asm-6.0.jar Copy DB_test.class.bk DB_test.class

