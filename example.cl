
(*Classes used to help testing *)
class D inherits B {
	alpha : D;
	temp : Int <- 4;
	negify(x : Int) : Int {
		~x
	};

};
class A inherits IO{
	print(x: Int) : Object
	{ {
	  out_int(x);
	  out_string("\n");
	  }
        };
};

class B inherits A {
	print(x: Int) : Object
	{ {
	  out_int(x+2);
	  out_string("\n");
	  }
        };
};

class C inherits B {
	x() : Object { 1};
};
class E inherits IO{
	a : Int;
	b : Bool;
	c : B <- new B;
	init(x : Int, y : Bool) : E {
           {
		a <- x;
		b <- y;
		c.print(x);
		self;
           }
	};
};
class F inherits A{
  foo() : Object {
        let thing : String <- "41" in
      case thing of
	i : Int => out_string( "int\n" );
	b : Bool => out_string( "bool\n" );
	s : String => {out_string(s); out_string("\n");};
      esac
  };
  bar() : Object {
    let x : Int <- 42 in 
      while x < 51 loop
        if x < 51 then
          {print(x); 
           x <- x + 1; }
        else
          new E
        fi
      pool
  };
  
};

(*Main testing, prints 1-50 ended with an abort *)
class Main inherits IO {
  i : Int <- 3;
  k : Int;
  l : Int <- 1;
  m : Int <- 13;
  a : A <- new A;
  b : B <- new B;
  d : D <- new D;
  e : String <- "123456789";
  f : C;
  razz : F;
  bazz : F;
  --e : C <- new C;
  main() : Object {	
	{
	(*Basic int and basic dispatch *)
	i;
	out_int(1); out_string("\n");
	a.print(2); 
	b.print(l); 
	
	(*arithmatic*)
	out_int(d.negify(4)); out_string("\n");
	out_int(2+3); out_string("\n");
	out_int(3*2); out_string("\n");
	out_int(10-i); out_string("\n");
	out_int(32/4); out_string("\n");

	(*String operations, methods within methods *)
	out_int(e.length()); out_string("\n");

	(*assign*)
	k <- 10;
	out_int(k); out_string("\n");

	(*isvoid, not false, conditionals*)
	if isvoid f then out_int(11) else out_int(12) fi; out_string("\n");
	if not false then out_int(12) else out_int(11) fi; out_string("\n");
	
	(*loop*)
        while m < 21 loop 
	  {
	    out_int(m);
	    m <- m+1;
	    out_string("\n");
	  }
	 pool;

	(*let*)
        let x : Int <- 4 in (new B).print(x*x+i);
	
	(*case*)
	case 22 of
		d : Bool => d;
		e : String => "yess";
		f : Int => { out_int(f); out_string("\n"); };
	esac;

	(*static dispatch*)
	b@A.print(23);

	(*new*)
	(new E).init(22,true);

	(*more complicated stuff *)
	if not i = m then 
	  {
	  i <- 25;
	  m <- i + (5*3);
	  while i <= m loop
	    {
	    out_int(i); out_string("\n");
	    i <- i + 1;
            } pool;
 	  } else 0 fi;
	 razz <- new F;
         razz.foo();
	 razz.bar();
         bazz.bar(); --bazz not initialized. dispatch_abort
	}
	
  };

}; 


