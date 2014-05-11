class A { --general tests
	a : Int;
	b : Bool;
	c: String;
	d : Int <- 1;
	e : Bool <- true;
	f : String <- "yes";
	foo() : Int { 1};
	gaa(x : Int) : Int { {x+2; x-2; 2*x; 2/x;}};
	hee(g : String) :String {g <- "curry" };		 
};
--inherit tests + plus some slightly complicated stuff
class B { 
	a : Int <- 1;
	bm() : Int { 10 };
};
class C inherits B { 
	teriyaki(y: B) : String{ {
	let x : Int <- 1 in { x <- 4; y.bm(); };
	"hello"; }
};
};
class D inherits C {
	sushi(x : Int) : Int {
	{
		a+12;
		bm();
	}
	};
};
--if and while
Class E{
	teste() : Object {
		if 1 < 2 then true else "chair" fi
	};
	testee(a : B, b : A, i : Int) : D {
		{
		while i <= 4 loop i <- i + 1 pool;
		(new D);
		}
	};
};
--let and case
Class F inherits E {
	cloud() : Object { {
		let x : Object in { x.copy(); }; }
	};
	butt(x : Int, y: Bool) : Int {
	{	case 1 of
		x : String => "hi"; 
		y : Bool => 1;
		esac;
		x; }
	};
	
};
--new, isvoid, comparison
Class G inherits F {
	k : Int <- 3;
	l : Bool <- not true;
	blah() : Int{
		butt(k,l)
	};
	bleh() : E {
		new E
	};
	bluh() : Bool {
		isvoid k
	};
};
--static dispatch and self
Class H inherits G {
	butt(a : Int, b : Bool) : Int {
		1
	};
	snowcone() : SELF_TYPE {
		self
	};
	bowow() : Int{
		self@F.butt(1, true)
	};
	phone() : Object { {
		let x : H in { x.snowcone(); }; }
	};
};

--larger miscellaenous programs


class I {
	a : Int <- 1;
	b : Bool <- true;
	init(x : Int, y : Bool) : I {
           {
		a <- 1;
		not false;
		b <- true;
		case 1 of
		x : String => "hi"; 
		y : Bool => 1;
		esac;
		--if 1 then 1 else 1 fi;
		self;
           }
	};
	foo() : Int {
		a
	};
	a() : Int { a};
	b(c : Bool) : Int { 1 };
}; 

class J {
    foo(a:Int, b: K, c:J, d:K) : SELF_TYPE {
       self
    };
  
};

class K inherits J {

    moo() : SELF_TYPE {
       let b:L <- new L in
         foo(4, b, b, b)
    };  
};

class L inherits K {
   foo:Int;
};

class Count {
   i : Int <- 0;
   inc () : SELF_TYPE {
        {
            i <- i + 1;
            self;
        }
    };
};  

class Stock inherits Count { 
   name : String; -- name of item
   get() :String {name };
};
class Foo inherits Bazz {
     a : Razz <- case self of
		      n : Razz => (new Bar);
		      n : Foo => (new Razz);
		      n : Bar => n;
   	         esac;

     b : Int <- a.doh() + g.doh() + doh() + printh();

     doh() : Int { (let i : Int <- h in { h <- h + 2; i; } ) };

};

class Bar inherits Razz {

     c : Int <- doh();

     d : Object <- printh();
};


class Razz inherits Foo {

     e : Bar <- case self of
		  n : Razz => (new Bar);
		  n : Bar => n;
		esac;

     f : Int <- a@Bazz.doh() + g.doh() + e.doh() + doh() + printh();

};

class Bazz inherits IO {

     h : Int <- 1;

     g : Foo  <- case self of
		     	n : Bazz => (new Foo);
		     	n : Razz => (new Bar);
			n : Foo  => (new Razz);
			n : Bar => n;
		  esac;

     i : Object <- printh();

     printh() : Int { { out_int(h); 0; } };

     doh() : Int { (let i: Int <- h in { h <- h + 1; i; } ) };
};


class Main {
  a : Bazz <- new Bazz;
  b : Foo <- new Foo;
  c : Razz <- new Razz;
  d : Bar <- new Bar;
  e:Stock;
  main(): Object {{5.copy(); "test".copy(); true.copy(); 
                  "test".length();   
		e <- (new Stock).inc (); 
    		e.get();  } };

};





