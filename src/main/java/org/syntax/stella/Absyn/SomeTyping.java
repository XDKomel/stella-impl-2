// File generated by the BNF Converter (bnfc 2.9.4.1).

package org.syntax.stella.Absyn;

public class SomeTyping  extends OptionalTyping {
  public final Type type_;
  public int line_num, col_num, offset;
  public SomeTyping(Type p1) { type_ = p1; }

  public <R,A> R accept(org.syntax.stella.Absyn.OptionalTyping.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof org.syntax.stella.Absyn.SomeTyping) {
      org.syntax.stella.Absyn.SomeTyping x = (org.syntax.stella.Absyn.SomeTyping)o;
      return this.type_.equals(x.type_);
    }
    return false;
  }

  public int hashCode() {
    return this.type_.hashCode();
  }


}
