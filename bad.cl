class Main {
a:Int <-0;
b:Int <-1;
c:String<-"hi";  
d:B <- new B;
f:A;
--Div by 0
main():Int {
      b/a
    };
--Case on void
test1() : Int {
    case f of 
	e : A => 0; 
	e : B => 0; 
	esac
    };
--No matching case
test2() : Int {
    case d of 
	e : A => 0;  
	esac
    };
--Dispatch to void
test4() : Int {
    f.test5()
    };
};
class A {
	test5():Int{0};
};

class B {
};