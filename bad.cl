
(*
 *  execute "coolc bad.cl" to see the error messages that the coolc parser
 *  generates
 *
 *  execute "./myparser bad.cl" to see the error messages that your parser
 *  generates
 *)
(*CLASS ERRORS*)

(* error:  c is not a type identifier *)
Class c inherits A {
};

(* error:  a is not a type identifier *)
Class C inherits a {
};

(* error:  keyword inherits is misspelled *)
Class C inherts A {
};

(* error:  closing brace is missing *)
Class C inherits A {
;

(* error: missing a closing  *)
Class C {
};

(*FEATURE ERRORS *)
(* error: A is not an object identifier *)
Class F {
    A() : Int {
        1
    };
};

(* error: b is not a type identifier *)
Class F {
    a() : b {
        1;
    };
};

(* error: extra colon  *)
Class F {
    a()  B {
       1;
    };
};

(* error: misisng opening bracket*)
Class F {
    a() : B 
       1
    };
};
(* error: missing closing bracket *)
Class F {
    a() : B {
       1
    };
};
(* error: A is not an object identifier *)
Class F {
    A : B
};
(* error: B is not a type identifier *)
Class F {
    a : b
};

(* error: B is not a type identifier *)
Class F {
    a : B 1
};

(* error: missing <- *)
Class F {
    a : B 1
};

(* error: missing expr *)
Class F {
    a : B <-
};

(* error: C is not a valid expr *)
Class F {
    a : B <- C
};
(* FORMAL ERRORS *)

(* error: C is not an object identifier  *)
Class F {
    a(C : D) : B {
       1
    };
};
(* error: d is not a type identifier  *)
Class F {
    a(c : d) : B {
       1
    };
};
(* error: missing a comma *)
Class F {
    a(c : d e : f) : B {
       1
    };
};
(* error: missing a semicolon  *)
Class F {
    a(c  d, e : f) : B {
       1
    };
};

(*EXPRESSION ERRORS *)
(*ASSIGN*)
(* error: C is not an object identifier  *)
Class A {
    d() : E {
        C <- 1
    };
};

(* error: E is not a valid expression *)
Class A {
    b() : C {
        d <- E
    };
};

(*DISPATCH ERRORS *)
(* error: A is not a type identifier *)
Class D {
    x() : Y {
        a.b()
    };
};
(* error: B is not an object identifier *)
Class D {
    x() : Y {
        A.B()
    };
};
(* error: C is not a valid expression *)
Class D {
    x() : Y {
        A.b(C)
    };
};
(* error: missing a comma *)
Class D {
    x() : Y {
        A.b(c d)
    };
};
(* error: b is not an object identifier *)
Class D {
    x() : Y {
        B(c, d)
    };
};
(* error: C is not a valid expression *)
Class D {
    x() : Y {
        b(C)
    };
};
(* error: B is not a type identifier *)
Class D {
    x() : Y {
        a@b.C()
    };
};
(* error: missing dot and object identifier *)
Class D {
    x() : Y {
        a@B()
    };
};
(* error: missing a type identifier *)
Class D {
    x() : Y {
        b@.a(c d)
    };
};
(*IF-STATEMENT ERRORS*)
(* error: A, B, C are not valid expressions *)
Class I {
    x() : Y {
        if A then B else C fi
    };
};
(* error: missing then *)
Class I {
    x() : Y {
        if a b else c fi
    };
};
(* error: missing else *)
Class I {
    x() : Y {
        if a then b<-1 fi
    };
};
(* error: missing expression *)
Class I {
    x() : Y {
        if then b<-1 else c 
    };
};

(*WHILE ERRORS *)
(* error: missing pool *)
Class W {
    x() : Y {
        while a loop b 
    };
};
(* error: missing loop *)
Class W {
    x() : Y {
        while a b pool 
    };
};
(* error: missing 2 expressiosn *)
Class W {
    x() : Y {
        while loop pool 
    };
};

(*BLock-STATEMENT ERRORS *)
(* error: missing semicolon *)
Class B {
    x() : Y {
        {a}
    };
};
(* error: missing closing bracket *)
Class B {
    x() : Y {
        {a; b
    };
};
(* error: missing opening bracket *)
Class B {
    x() : Y {
        a; b; } 
    };
};

(*LET ERRORS *)
(* error: missing in <body> *)
Class L {
    x() : Y {
        let a : B <- c 
    };
};
(* error: A is not an object and b is not a type *)
Class L {
    x() : Y {
        let A : b in c
    };
};

(* error: missing comma *)
Class L {
    x() : Y {
        let a : B d : E <- f in c
    };
};

(*CASE ERRORS*)
(* error: missing esac *)
Class C {
    x() : Y {
        case a of
            b : C => d;
        
    };
};
(* error: missing of *)
Class C {
    x() : Y {
        case a 
            b : C => d;
        esac
    };
};
(* error: <- is not => *)
Class C {
    x() : Y {
        case a of
            b : C <- d;
        esac
    };
};
(* error: missing ; *)
Class C {
    x() : Y {
        case a of
            b : C <- d
            e : F <- g
        esac
    };
};
(* error: Each type and object is of the wrong type *)
Class C {
    x() : Y {
        case a of
            B : c <- d;
            E : f <- g;
        esac
    };
};
(* error: D and G are not valid expressions *)
Class C {
    x() : Y {
        case a of
            B : c <- D;
            E : f <- G;
        esac
    };
};

(*NEW ERRORS*)
(* error: a is not a type identifier and new is mispelled*)
Class N {
    x() : Y {
        new a
    };
    x() : Y {
        nwe a
    };
};
(*ISVOID ERRORS*)
(* error: a is not an expression and isvoid is mispelled*)
Class V {
    x() : Y {
        isvoid A
    };
    x() : Y {
        isviod a
    };
};
(*ARITHMETIC ERRORS*)
(* error: A and B re not valid expressions*)
Class V {
    x() : Y {
        A+B
    };
    x() : Y {
        A-B
    };
    x() : Y {
        A*B
    };
    x() : Y {
        A/B
    };
    x() : Y {
        ~A
    };
};
(*COMPARISON ERRORS*)
(* error: A and B re not valid expressions*)
(* error: =< is not <= *)
Class V {
    x() : Y {
        A<B
    };
    x() : Y {
        A<=B
    };
    x() : Y {
        A=<B
    };
    x() : Y {
        A=B
    };
};
(*NOT ERRORS*)
(* error: A is not an expression and not is mispelled*)
Class V {
    x() : Y {
        not A
    };
    x() : Y {
        nto a
    };
};
(*PARENTHESIS ERRORS*)
(* error: Missing parens and wrong parens*)
Class V {
    x() : Y {
        (a
    };
    x() : Y {
        a)
    };
    x() : Y {
        )a(
    };
};
(*OBJECT, INT, STRING, BOOL ERRORS*)
(* error: read -- comments*)
Class V {
    x() : Y {
        A   --A is not an object identifier
    };
    x() : Y {
        1a2     --1a2 is not a valid int
    };
    x() : Y {
        "ba     --missing closing "
    };
    x() : Y {
        "ba     --missing opening "
    };
   x() : Y {
        True     --capitalized t
    };
   x() : Y {
        False     --capitalized f
    };
   x() : Y {
        teur     --mispelled true
    };
   x() : Y {
        flsae     --mispelled false
    };
};