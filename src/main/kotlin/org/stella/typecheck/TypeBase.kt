package org.stella.typecheck

class TypeBase<T> {
    private val base = HashMap<String, ArrayDeque<T>>()

    fun put(key: String, value: T) {
        if (!this.base.contains(key)) {
            this.base[key] = ArrayDeque()
        }
        this.base[key]?.addFirst(value)
    }

    fun put(keys: List<String>, values: List<T>) {
        keys.zip(values).forEach { put(it.first, it.second) }
    }

    fun take(key: String): T {
        if (!this.base.contains(key)) {
            throw NoSuchElementException()
        } else if (this.base[key]!!.isEmpty()) {
            throw EmptyDequeException()
        }
        return this.base[key]?.first()!!
    }

    fun remove(keys: List<String>) {
        for (key in keys) {
            this.remove(key)
        }
    }

    fun remove(key: String) {
        if (!this.base.contains(key)) {
            throw NoSuchElementException()
        } else if (this.base[key]!!.isEmpty()) {
            throw EmptyDequeException()
        }
        this.base[key]?.removeFirst()
    }
}