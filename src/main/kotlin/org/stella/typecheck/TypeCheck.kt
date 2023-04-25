package org.stella.typecheck

import org.syntax.stella.Absyn.*
import org.syntax.stella.PrettyPrinter
import java.util.LinkedList
import kotlin.sequences.Sequence

class EmptyDequeException: Exception()

object TypeCheck {
    private val nat = TypeNat()
    private val bool = TypeBool()
    private val unit = TypeUnit()
    private val varTypeBase = TypeBase<Type>()
    private fun constructRecNatExpr3(type: Type): TypeFun {
        return TypeFun(ListType().let {
            it.add(nat)
            it
        }, TypeFun(ListType().let {
            it.add(type)
            it
        }, type))
    }

    private fun unwrapReturnType(type: ReturnType, decl: Decl): Type = when (type) {
        is SomeReturnType -> type.type_
        else -> throw Exception("Met NoReturnType at \n${PrettyPrinter.print(decl)}\n")
    }

    private fun checkTypes(type1: Type, type2: Type, expr: Expr) {
        fun equalTypeSum(type1: TypeSum, type2: TypeSum): Boolean {
            fun equalNode(one: Type?, two: Type?): Boolean {
                return if (one is TypeSum && two is TypeSum) {
                    equalTypeSum(one, two)
                } else if (one != null && two != null) {
                    one == two
                } else true
            }
            return equalNode(type1.type_1, type2.type_1) &&
                    equalNode(type1.type_2, type2.type_2)
        }

        fun isRecordSubtype(subtype: TypeRecord, supertype: TypeRecord): Boolean {
            return subtype.listrecordfieldtype_.toSet().containsAll(supertype.listrecordfieldtype_.toSet())
        }

        fun equalTypeFun(type1: TypeFun, type2: TypeFun): Boolean {
            checkTypes(type1.type_, type2.type_, expr)
            type1.listtype_.zip(type2.listtype_).map { checkTypes(it.second, it.first, expr) }
            return true
        }

        val typeSumEqual = (
            type1 is TypeSum &&
            type2 is TypeSum &&
            equalTypeSum(type1, type2))

        val typeRecordEqual = (
            type1 is TypeRecord &&
            type2 is TypeRecord &&
            isRecordSubtype(type1, type2))

        val typeFunEqual = (
            type1 is TypeFun &&
            type2 is TypeFun &&
            equalTypeFun(type1, type2))

        if ((typeFunEqual || typeRecordEqual || typeSumEqual || type1 == type2).not())
            throw Exception("Incorrect type for the \n${PrettyPrinter.print(expr)}\nGot $type1 and $type2")
    }

    private fun getVarType(expr: Var): Type {
        try {
            return varTypeBase.take(expr.stellaident_)
        } catch (e: NoSuchElementException) {
            throw Exception("Refer to an unknown variable ${expr.stellaident_} at \n${PrettyPrinter.print(expr)}\n")
        } catch (e: EmptyDequeException) {
            throw Exception("Refer to a variable ${expr.stellaident_} from a local scope that is currently unavailable at \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getApplicationType(expr: Application): Type {
        when (val typeFun = getExprType(expr.expr_)) {
            is TypeFun -> {
                for (p in typeFun.listtype_.zip(expr.listexpr_)) {
                    checkTypes(getExprType(p.second, p.first), p.first, expr)
                }
                return typeFun.type_
            }
            else -> throw Exception("Calling something which is not a function \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getSuccType(expr: Succ): Type {
        val t = getExprType(expr.expr_, nat)
        checkTypes(t, nat, expr)
        return nat
    }

    private fun getIfType(expr: If, awaiting: Type?): Type {
        checkTypes(getExprType(expr.expr_1, bool), bool, expr)
        val type1 = getExprType(expr.expr_2, awaiting)
        val type2 = getExprType(expr.expr_3, awaiting)
        checkTypes(type1, type2, expr)
        return type1
    }

    private fun getNatRecType(expr: NatRec): Type {
        checkTypes(getExprType(expr.expr_1, nat), nat, expr)
        val t = getExprType(expr.expr_2)
        val recNatType = constructRecNatExpr3(t)
        checkTypes(getExprType(expr.expr_3, recNatType), recNatType, expr)
        return t
    }

    private fun getIsZeroType(expr: IsZero): Type {
        checkTypes(getExprType(expr.expr_), nat, expr)
        return bool
    }

    private fun getAddType(expr: Add): Type {
        checkTypes(nat, getExprType(expr.expr_1, nat), expr.expr_1)
        checkTypes(nat, getExprType(expr.expr_2, nat), expr.expr_2)
        return nat
    }

    private fun getSubtractType(expr: Subtract): Type {
        checkTypes(nat, getExprType(expr.expr_1, nat), expr.expr_1)
        checkTypes(nat, getExprType(expr.expr_2, nat), expr.expr_2)
        return nat
    }
    private fun getMultiplyType(expr: Multiply): Type {
        checkTypes(nat, getExprType(expr.expr_1, nat), expr.expr_1)
        checkTypes(nat, getExprType(expr.expr_2, nat), expr.expr_2)
        return nat
    }

    private fun getDivideType(expr: Divide): Type {
        checkTypes(nat, getExprType(expr.expr_1, nat), expr.expr_1)
        checkTypes(nat, getExprType(expr.expr_2, nat), expr.expr_2)
        return nat
    }

    private fun getTupleType(expr: Tuple): Type {
        return TypeTuple(ListType().let { types ->
            for (type in expr.listexpr_.map { getExprType(it) }) {
                types.add(type)
            }
            types
        })
    }

    private fun getDotTupleType(expr: DotTuple): Type {
        return when (val exprType = getExprType(expr.expr_)) {
            is TypeTuple -> {
                exprType.listtype_[expr.integer_-1]
            }
            else -> throw Exception("Try to access a value by number from a non-tuple type at \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getInlType(expr: Inl): Type {
        return TypeSum(getExprType(expr.expr_), null)
    }

    private fun getInrType(expr: Inr): Type {
        return TypeSum(null, getExprType(expr.expr_))
    }

    private fun applyPattern(exprType: Type, pattern: Pattern, returnExpr: Expr, awaiting: Type?): Type {
        return when (pattern) {
            is PatternInl -> {
                if (exprType is TypeSum)
                    applyPattern(exprType.type_1, pattern.pattern_, returnExpr, awaiting)
                else throw Exception("Try to apply inl-pattern to $exprType at $pattern")
            }
            is PatternInr -> {
                if (exprType is TypeSum)
                    applyPattern(exprType.type_2, pattern.pattern_, returnExpr, awaiting)
                else throw Exception("Try to apply inr-pattern to $exprType at $pattern")
            }
            is PatternVar -> {
                varTypeBase.put(pattern.stellaident_, exprType)
                val returnType = getExprType(returnExpr, awaiting)
                varTypeBase.remove(pattern.stellaident_)
                return returnType
            }
            else -> throw Exception("Unknown pattern at \n${PrettyPrinter.print(pattern)}\n")
        }
    }

    private fun commonNullableType(x: Type?, y: Type?): Type? {
        return if (x == null) y
        else if (y == null) x
        else commonType(x, y)
    }

    private fun commonType(x: Type, y: Type): Type {
        return if (x is TypeSum && y is TypeSum) {
            TypeSum(
                commonNullableType(x.type_1, y.type_1),
                commonNullableType(x.type_2, y.type_2))
        } else if (x == y) {
            x
        } else {
            throw Exception("Couldn't find a common type for ${PrettyPrinter.print(x)} and ${PrettyPrinter.print(y)}")
        }
    }

    private fun getMatchType(expr: Match, awaiting: Type?): Type {
        val exprType = getExprType(expr.expr_)
        val types = expr.listmatchcase_.map { matchCase ->
            when (matchCase) {
                is AMatchCase -> applyPattern(exprType, matchCase.pattern_, matchCase.expr_, awaiting)
                else -> throw Exception("Unknown type for a match statement at \n${PrettyPrinter.print(expr)}\n")
            }
        }
        return types.drop(1).fold(types[0]) { next, res ->
            commonType(next, res)
        }
    }

    private fun getRecordType(expr: Record): Type {
        return TypeRecord(ListRecordFieldType().let { recordList ->
            expr.listbinding_.forEach {
                when (it) {
                    is ABinding -> recordList.add(ARecordFieldType(it.stellaident_, getExprType(it.expr_)))
                    else -> throw Exception("Unknown type for a binding at \n${PrettyPrinter.print(expr)}\n")
                }
            }
            recordList
        })
    }

    private fun getDotRecordType(expr: DotRecord): Type {
        when (val exprType = getExprType(expr.expr_)) {
            is TypeRecord -> {
                exprType.listrecordfieldtype_.forEach {
                    when (it) {
                        is ARecordFieldType -> {
                            if (expr.stellaident_ == it.stellaident_)
                                return it.type_
                        }
                        else -> throw Exception("Try to access a record field $it of unknown type at \n${PrettyPrinter.print(expr)}\n")
                    }
                }
            }
            else -> throw Exception("Try to access a value by name from a non-record type at \n${PrettyPrinter.print(expr)}\n")
        }
        throw Exception("Try to access a record field with non-existent name ${expr.stellaident_} at \n${PrettyPrinter.print(expr)}\n")
    }

    private fun getListType(expr: org.syntax.stella.Absyn.List): Type {
        expr.listexpr_.map { getExprType(it) }.let {
            if (it.toSet().size == 1 )
                return TypeList(it[0])
            throw Exception("A list ${PrettyPrinter.print(expr)} contains elements of different types")
        }
    }

    private fun getIsEmptyType(expr: IsEmpty): Type {
        when (getExprType(expr.expr_)) {
            is TypeList -> return bool
            else -> throw Exception("Try to apply isEmpty on a non-list type at \n${PrettyPrinter.print(expr.expr_)}\n")
        }
    }

    private fun getHeadType(expr: Head): Type {
        when (val listType = getExprType(expr.expr_)) {
            is TypeList -> return listType.type_
            else -> throw Exception("Try to apply head on a non-list type at \n${PrettyPrinter.print(expr.expr_)}\n")
        }
    }

    private fun getTailType(expr: Tail): Type {
        when (val listType = getExprType(expr.expr_)) {
            is TypeList -> return TypeList(listType.type_)
            else -> throw Exception("Try to apply tail on a non-list type at \n${PrettyPrinter.print(expr.expr_)}\n")
        }
    }

    private fun getLetType(expr: Let, awaiting: Type?): Type {
        if (expr.listpatternbinding_.isEmpty()) {
            return getExprType(expr.expr_, awaiting)
        }
        val bindings = ListPatternBinding().let { bindings ->
            expr.listpatternbinding_.drop(1).forEach {
                bindings.add(it)
            }
            bindings
        }
        when (val cur = expr.listpatternbinding_[0]) {
            is APatternBinding -> {
                return applyPattern(getExprType(cur.expr_), cur.pattern_, Let(bindings, expr.expr_), awaiting)
            }
            else -> throw Exception("Try to use let with a non-Let type at \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getEqualType(expr: Equal, awaiting: Type?): Type {
        checkTypes(getExprType(expr.expr_1, awaiting), getExprType(expr.expr_2, awaiting), expr)
        return bool
    }

    private fun getPanicType(expr: Panic, awaiting: Type?): Type {
        if (awaiting == null) {
            throw Exception("No context to imply the Panic! type at \n${PrettyPrinter.print(expr)}\n")
        }
        return awaiting
    }

    private fun getRefType(expr: Ref, awaiting: Type?): Type {
        return when (awaiting) {
            is TypeRef -> TypeRef(getExprType(expr.expr_, awaiting.type_))
            null -> TypeRef(getExprType(expr.expr_))
            else -> throw Exception("Awaiting a non reference type from a reference at \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getDerefType(expr: Deref, awaiting: Type?): Type {
        return when (val reference = getExprType(expr.expr_, awaiting)) {
            is TypeRef -> reference.type_
            else -> throw Exception("Try to dereference a non reference type at \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getAssignType(expr: Assign): Type {
        val t = getExprType(expr.expr_2)
        checkTypes(getExprType(expr.expr_1), TypeRef(t), expr)
        return unit
    }

    private fun getSequenceType(expr: org.syntax.stella.Absyn.Sequence, awaiting: Type?): Type {
        checkTypes(getExprType(expr.expr_1, unit), unit, expr.expr_1)
        return getExprType(expr.expr_2, awaiting)
    }

    private fun getExprType(expr: Expr, awaiting: Type? = null): Type = when (expr) {
        is Var -> getVarType(expr)
        is ConstTrue -> bool
        is ConstFalse -> bool
        is ConstInt -> nat
        is ConstUnit -> unit
        is Succ -> getSuccType(expr)
        is If -> getIfType(expr, awaiting)
        is NatRec -> getNatRecType(expr)
        is IsZero -> getIsZeroType(expr)
        is Add -> getAddType(expr)
        is Subtract -> getSubtractType(expr)
        is Multiply -> getMultiplyType(expr)
        is Divide -> getDivideType(expr)
        is Abstraction -> getAbstractionType(expr, awaiting)
        is Application -> getApplicationType(expr)
        is Tuple -> getTupleType(expr)
        is DotTuple -> getDotTupleType(expr)
        is Inl -> getInlType(expr)
        is Inr -> getInrType(expr)
        is Match -> getMatchType(expr, awaiting)
        is Record -> getRecordType(expr)
        is DotRecord -> getDotRecordType(expr)
        is org.syntax.stella.Absyn.List -> getListType(expr)
        is IsEmpty -> getIsEmptyType(expr)
        is Head -> getHeadType(expr)
        is Tail -> getTailType(expr)
        is Let -> getLetType(expr, awaiting)
        is Equal -> getEqualType(expr, awaiting)
        is Panic -> getPanicType(expr, awaiting)
        is Ref -> getRefType(expr, awaiting)
        is Deref -> getDerefType(expr, awaiting)
        is Assign -> getAssignType(expr)
        is org.syntax.stella.Absyn.Sequence -> getSequenceType(expr, awaiting)
        else -> throw Exception("Unknown type at \n${PrettyPrinter.print(expr)}\n")
    }

    private fun getAbstractionType(expr: Abstraction, awaiting: Type?): TypeFun {
        return when (awaiting) {
            is TypeFun -> getTypeFun(expr.expr_, getParamTypes(expr.listparamdecl_), awaiting.type_)
            null -> getTypeFun(expr.expr_, getParamTypes(expr.listparamdecl_), null)
            else -> throw Exception("Awaiting a non function type from an abstraction at \n${PrettyPrinter.print(expr)}\n")
        }
    }

    private fun getParamTypes(params: ListParamDecl): Pair<LinkedList<String>, ListType> {
        val pair = Pair<LinkedList<String>, ListType>(LinkedList(), ListType())
        params.forEach { param ->
            when (param) {
                is AParamDecl -> {
                    pair.first.add(param.stellaident_)
                    pair.second.add(param.type_)
                }
            }
        }
        return pair
    }

    private fun getTypeFun(expr: Expr, params: Pair<LinkedList<String>, ListType>, awaiting: Type?): TypeFun {
        varTypeBase.put(params.first, params.second)
        val type = TypeFun(params.second, getExprType(expr, awaiting))
        varTypeBase.remove(params.first)
        return type
    }

    private fun typecheckDeclFun(decl: DeclFun) {
        val returnType = unwrapReturnType(decl.returntype_, decl)
        val params = getParamTypes(decl.listparamdecl_)
        varTypeBase.put(decl.stellaident_, TypeFun(params.second, returnType))
        val type = getTypeFun(decl.expr_, params, returnType)
//        varTypeBase.remove(decl.stellaident_)
        if (type.type_ != returnType)
            throw Exception("Function declaration \n${PrettyPrinter.print(decl)}\n has a return type \n${PrettyPrinter.print(type)}\n that does not match the intended \n${PrettyPrinter.print(returnType)}\n")
    }

    @Throws(Exception::class)
    fun typecheckProgram(program: Program?) {
        when (program) {
            is AProgram ->
                for (decl in program.listdecl_) {
                    when (decl) {
                        is DeclFun -> typecheckDeclFun(decl)
                    }
                }
        }
        return
    }
}
