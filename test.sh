#!/bin/sh

cat "package GeneralRegFileToBRAM" > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedAdd.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/add.sc/ir/chisel-AddTest.scala /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedAdd.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedAddTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedBubble.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/bubble.sc/ir/chisel-BubbleTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedBubble.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedBubbleTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedConstruct.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/construct.sc/ir/chisel-MkISTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedConstruct.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedConstructTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedDivRem.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/divrem.sc/ir/chisel-DivRemTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedDivRem.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedDivRemTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedFactorial.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/factorial.sc/ir/chisel-FactorialTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedFactorial.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedFactorialTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedGlobal.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/global.sc/ir/chisel-GlobalTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedGlobal.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedGlobalTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedInstanceof.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/instanceof.sc/ir/chisel-InstanceofTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedInstanceof.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedInstanceofTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedLocal.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/local-reuse.sc/ir/chisel-LocalReuseTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedLocal.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedLocalTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedMult.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/mult.sc/ir/chisel-MultTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedMult.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedMultTestBench"

echo "package GeneralRegFileToBRAM" | > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedPrintlnU64.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/printU64.sc/ir/chisel-PrintlnU64Test.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedPrintlnU64.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedPrintlnU64TestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedSquare.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/seq.sc/ir/chisel-SquareTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedSquare.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedSquareTestBench"

echo "package GeneralRegFileToBRAM" | cat > /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedSum.scala
cat /home/kejun/git_dir/Sireum/anvil/jvm/result/sum.sc/ir/chisel-SumTest.scala >> /home/kejun/git_dir/chisel-template/src/main/scala/GeneralRegFileToBRAM/GeneratedSum.scala
VL_THREADS=16 sbt "testOnly *GeneralRegFileToBRAM.GeneratedSumTestBench"
