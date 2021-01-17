[TOC]

## 简介

什么是guava？（番石榴）

Guava是一种基于开源的Java库，其中包含谷歌正在由他们很多项目使用的很多核心库。这个库是为了方便编码，并减少编码错误。这个库提供用于集合，缓存，支持原语，并发性，常见注解，字符串处理，I/O和验证的实用方法。

简而言之，guava是一个帮我们优雅地实现了某些自行实现起来很麻烦的功能的类库。

## 1. 基本工具 [Basic utilities]

让使用Java语言变得更舒适

#### 1.1 使用和避免null

null是模棱两可的，会引起令人困惑的错误，有些时候它让人很不舒服。很多Guava工具类用快速失败拒绝null值，而不是盲目地接受。

Null很少可以明确地表示某种语义，例如，Map.get(key)返回Null时，可能表示map中的值是null，亦或map中没有key对应的值。

Guava用Optional表示可能为null的T类型引用。一个Optional实例可能包含**非null的引用**（我们称之为引用存在），也可能什么也不包括（称之为引用缺失）。它从不说包含的是null值，而是用存在或缺失来表示。但Optional从不会包含null值引用。

```java
Optional<Integer> possible = Optional.formNullable(5);

possible.isPresent(); // returns true

possible.get(); // returns 5
```

使用Optional除了赋予null语义，增加了可读性，最大的优点在于它是一种**傻瓜式**的防护。Optional迫使你积极思考引用缺失的情况，因为你必须显式地从Optional获取引用。直接使用null很容易让人忘掉某些情形，尽管FindBugs可以帮助查找null相关的问题，但是我们还是认为它并不能准确地定位问题根源。

#### 1.2 前置条件:

让方法中的条件检查更简单

Guava在Preconditions类中提供了若干前置条件判断的实用方法。每个方法都有三个变种：

- 没有额外参数：抛出的异常中没有错误消息；

- 有一个Object对象作为额外参数：抛出的异常使用Object.toString() 作为错误消息；

- 有一个String对象作为额外参数，并且有一组任意数量的附加Object对象

  | **方法声明（不包括额外参数）**                       | **描述**                                                     | **检查失败时抛出的异常**  |
  | ---------------------------------------------------- | ------------------------------------------------------------ | ------------------------- |
  | `checkArgument(boolean)`                             | 检查boolean是否为true，用来检查传递给方法的参数。            | IllegalArgumentException  |
  | `checkNotNull(T)`                                    | 检查value是否为null，该方法直接返回value，因此可以内嵌使用checkNotNull`。` | NullPointerException      |
  | `checkState(boolean)`                                | 用来检查对象的某些状态。                                     | IllegalStateException     |
  | `checkElementIndex(int index, int size)`             | 检查index作为索引值对某个列表、字符串或数组是否有效。index>=0 && index<size * | IndexOutOfBoundsException |
  | `checkPositionIndex(int index, int size)`            | 检查index作为位置值对某个列表、字符串或数组是否有效。index>=0 && index<=size * | IndexOutOfBoundsException |
  | `checkPositionIndexes(int start, int end, int size)` | 检查[start, end]表示的位置范围对某个列表、字符串或数组是否有效* | IndexOutOfBoundsException |

索引值常用来查找列表、字符串或数组中的元素，如**List.get(int), String.charAt(int)*

位置值和位置范围常用来截取列表、字符串或数组，如List.subList(int，int), String.substring(int)*

#### 1.3 常见Object方法:

简化Object方法实现，如hashCode()和toString()

**equals**

当一个对象中的字段可以为null时，实现Object.equals方法会很痛苦，因为不得不分别对它们进行null检查。使用`Objects.equal`帮助你执行null敏感的equals判断，从而避免抛出NullPointerException。例如:

```java
Objects.equal("a", "a"); // returns true
Objects.equal(null, "a"); // returns false
Objects.equal("a", null); // returns false
Objects.equal(null, null); // returns true
```

*注意：JDK7引入的Objects类提供了一样的方法`Objects.equals`*。



**hashCode**

用对象的所有字段作散列[hash]运算应当更简单。Guava的`Objects.hashCode(Object...)`会对传入的字段序列计算出合理的、顺序敏感的散列值。你可以使用Objects.hashCode(field1, field2, …, fieldn)来代替手动计算散列值。

*注意：JDK7引入的Objects类提供了一样的方法*`Objects.hash(Object...)`

**toString**

好的toString方法在调试时是无价之宝，但是编写toString方法有时候却很痛苦。使用 Objects.toStringHelper可以轻松编写有用的toString方法。例如：

```java
// Returns "ClassName{x=1}"
Objects.toStringHelper(this).add("x", 1).toString();
// Returns "MyObject{x=1}"
Objects.toStringHelper("MyObject").add("x", 1).toString();
```

**compare/compareTo**

实现一个比较器[Comparator]，或者直接实现Comparable接口有时也伤不起。考虑一下这种情况：

```java
class Person implements Comparable<Person> {
  private String lastName;
  private String firstName;
  private int zipCode;

  public int compareTo(Person other) {
    int cmp = lastName.compareTo(other.lastName);
    if (cmp != 0) {
      return cmp;
    }
    cmp = firstName.compareTo(other.firstName);
    if (cmp != 0) {
      return cmp;
    }
    return Integer.compare(zipCode, other.zipCode);
  }
}
```

这部分代码太琐碎了，因此很容易搞乱，也很难调试。我们应该能把这种代码变得更优雅，为此，Guava提供了`ComparisonChain`

ComparisonChain执行一种懒比较：它执行比较操作直至发现非零的结果，在那之后的比较输入将被忽略。

```java
public int compareTo(Foo that) {
    return ComparisonChain.start()
            .compare(this.aString, that.aString)
            .compare(this.anInt, that.anInt)
            .compare(this.anEnum, that.anEnum, Ordering.natural().nullsLast())
            .result();
}
```

这种Fluent接口风格的可读性更高，发生错误编码的几率更小，并且能避免做不必要的工作。更多Guava排序器工具可以在下一节里找到。

#### 1.4 排序器：

排序器[Ordering]是 Guava 流畅风格比较器[Comparator]的实现，它可以用来为构建复杂的比较器，以完成集合排序的功能。

从实现上说，Ordering 实例就是一个特殊的 Comparator 实例。Ordering 把很多基于 Comparator 的静态方法（如 Collections.max）包装为自己的实例方法（非静态方法），并且提供了链式调用方法，来定制和增强现有的比较器。

**创建排序器**：常见的排序器可以由下面的静态方法创建

| **方法**           | **描述**                                               |
| ------------------ | ------------------------------------------------------ |
| `natural()`        | 对可排序类型做自然排序，如数字按大小，日期按先后排序   |
| `usingToString()`  | 按对象的字符串形式做字典排序[lexicographical ordering] |
| `from(Comparator)` | 把给定的Comparator转化为排序器                         |

实现自定义的排序器时，除了用上面的from方法，也可以跳过实现Comparator，而直接继承Ordering：

```java
Ordering<String> byLengthOrdering = new Ordering<String>() {
  public int compare(String left, String right) {
    return Ints.compare(left.length(), right.length());
  }
};
```

**链式调用方法**：通过链式调用，可以由给定的排序器衍生出其它排序器

| **方法**               | **描述**                                                     |
| ---------------------- | ------------------------------------------------------------ |
| `reverse()`            | 获取语义相反的排序器                                         |
| `nullsFirst()`         | 使用当前排序器，但额外把null值排到最前面。                   |
| `nullsLast()`          | 使用当前排序器，但额外把null值排到最后面。                   |
| `compound(Comparator)` | 合成另一个比较器，以处理当前排序器中的相等情况。             |
| `lexicographical()`    | 基于处理类型T的排序器，返回该类型的可迭代对象Iterable<T>的排序器。 |
| `onResultOf(Function)` | 对集合中元素调用Function，再按返回值用当前排序器排序。       |

#### 1.5 Throwables：

有时候，你会想把捕获到的异常再次抛出。这种情况通常发生在Error或RuntimeException被捕获的时候，你没想捕获它们，但是声明捕获Throwable和Exception的时候，也包括了了Error或RuntimeException。Guava提供了若干方法，来判断异常类型并且重新传播异常。例如：

```java
try {
    someMethodThatCouldThrowAnything();
} catch (IKnowWhatToDoWithThisException e) {
    handle(e);
} catch (Throwable t) {
    Throwables.propagateIfInstanceOf(t, IOException.class);
    Throwables.propagateIfInstanceOf(t, SQLException.class);
    throw Throwables.propagate(t);
}
```

## 2. 集合[Collections]

Guava对JDK集合的扩展，这是Guava最成熟和为人所知的部分

#### 2.1 不可变集合

不可变对象有很多优点，包括：

- 当对象被不可信的库调用时，不可变形式是安全的；
- 不可变对象被多个线程调用时，不存在竞态条件问题
- 不可变集合不需要考虑变化，因此可以节省时间和空间。所有不可变的集合都比它们的可变形式有更好的内存利用率（分析和测试细节）；
- 不可变对象因为有固定不变，可以作为常量来安全使用。

不可变集合可以用如下多种方式创建：

- copyOf 方法，如 ImmutableSet.copyOf(set);
- of 方法，如 ImmutableSet.of(“a”, “b”, “c”)或 ImmutableMap.of(“a”, 1, “b”, 2);
- Builder 工具，如

```java
    public static final ImmutableSet<Color> GOOGLE_COLORS =
            ImmutableSet.<Color>builder()
                .addAll(WEBSAFE_COLORS)
                .add(new Color(0, 191, 255))
                .build();
```

此外，对有序不可变集合来说，排序是在构造集合的时候完成的，如：

```java
    ImmutableSortedSet.of("a", "b", "c", "a", "d", "b");
```

会在构造时就把元素排序为 a, b, c, d。

**关联可变集合和不可变集合**

| **可变集合接口**       | **属于**JDK**还是**Guava | **不可变版本**                |
| ---------------------- | ------------------------ | ----------------------------- |
| Collection             | JDK                      | `ImmutableCollection`         |
| List                   | JDK                      | `ImmutableList`               |
| Set                    | JDK                      | `ImmutableSet`                |
| SortedSet/NavigableSet | JDK                      | `ImmutableSortedSet`          |
| Map                    | JDK                      | `ImmutableMap`                |
| SortedMap              | JDK                      | `ImmutableSortedMap`          |
| Multiset               | Guava                    | `ImmutableMultiset`           |
| SortedMultiset         | Guava                    | `ImmutableSortedMultiset`     |
| Multimap               | Guava                    | `ImmutableMultimap`           |
| ListMultimap           | Guava                    | `ImmutableListMultimap`       |
| SetMultimap            | Guava                    | `ImmutableSetMultimap`        |
| BiMap                  | Guava                    | `ImmutableBiMap`              |
| ClassToInstanceMap     | Guava                    | `ImmutableClassToInstanceMap` |
| Table                  | Guava                    | `ImmutableTable`              |

#### 2.2 新集合类型

Guava 引入了很多 JDK 没有的、但我们发现明显有用的新集合类型。这些新类型是为了和 JDK 集合框架共存，而没有往 JDK 集合抽象中硬塞其他概念。作为一般规则，Guava 集合非常精准地遵循了 JDK 接口契约。

> ##### Multiset

统计一个词在文档中出现了多少次，传统的做法是这样的：

```java
Map<String, Integer> counts = new HashMap<String, Integer>();
for (String word : words) {
    Integer count = counts.get(word);
    if (count == null) {
        counts.put(word, 1);
    } else {
        counts.put(word, count + 1);
    }
}
```

这种写法很笨拙，也容易出错，并且不支持同时收集多种统计信息，如总词数。我们可以做的更好。

Guava提供了一个新集合类型 Multiset，它可以多次添加相等的元素。维基百科从数学角度这样定义Multiset：”集合[set]概念的延伸，它的元素可以重复出现…与集合[set]相同而与元组[tuple]相反的是，Multiset元素的顺序是无关紧要的：Multiset {a, a, b}和{a, b, a}是相等的”。

可以用两种方式看待Multiset：

- 没有元素顺序限制的ArrayList<E>
- Map<E, Integer>，键为元素，值为计数

Guava的Multiset API也结合考虑了这两种方式：
当把Multiset看成普通的Collection时，它表现得就像无序的ArrayList：

- add(E)添加单个给定元素
- iterator()返回一个迭代器，包含Multiset的所有元素（包括重复的元素）
- size()返回所有元素的总个数（包括重复的元素）

当把Multiset看作Map<E, Integer>时，它也提供了符合性能期望的查询操作：

- count(Object)返回给定元素的计数。HashMultiset.count的复杂度为O(1)，TreeMultiset.count的复杂度为O(log n)。
- entrySet()返回Set<Multiset.Entry<E>>，和Map的entrySet类似。
- elementSet()返回所有不重复元素的Set<E>，和Map的keySet()类似。
- 所有Multiset实现的内存消耗随着不重复元素的个数线性增长。

| **方法**         | **描述**                                                     |
| ---------------- | ------------------------------------------------------------ |
| count(E)         | 给定元素在Multiset中的计数                                   |
| elementSet()     | Multiset中不重复元素的集合，类型为Set<E>                     |
| entrySet()       | 和Map的entrySet类似，返回Set<Multiset.Entry<E>>，其中包含的Entry支持getElement()和getCount()方法 |
| add(E, int)      | 增加给定元素在Multiset中的计数                               |
| remove(E, int)   | 减少给定元素在Multiset中的计数                               |
| setCount(E, int) | 设置给定元素在Multiset中的计数，不可以为负数                 |
| size()           | 返回集合元素的总个数（包括重复的元素）                       |

**Multiset不是Map**

请注意，Multiset<E>不是Map<E, Integer>，虽然Map可能是某些Multiset实现的一部分。准确来说Multiset是一种Collection类型，并履行了Collection接口相关的契约。关于Multiset和Map的显著区别还包括：

- Multiset中的元素计数只能是正数。任何元素的计数都不能为负，也不能是0。elementSet()和entrySet()视图中也不会有这样的元素。
- multiset.size()返回集合的大小，等同于所有元素计数的总和。对于不重复元素的个数，应使用elementSet().size()方法。（因此，add(E)把multiset.size()增加1）
- multiset.iterator()会迭代重复元素，因此迭代长度等于multiset.size()。
- Multiset支持直接增加、减少或设置元素的计数。setCount(elem, 0)等同于移除所有elem。
- 对multiset 中没有的元素，multiset.count(elem)始终返回0。

**Multiset的各种实现**

Guava提供了多种Multiset的实现，大致对应JDK中Map的各种实现：

| **Map**           | **对应的****Multiset** | **是否支持****null****元素** |
| ----------------- | ---------------------- | ---------------------------- |
| HashMap           | HashMultiset           | 是                           |
| TreeMap           | TreeMultiset           | 是（如果comparator支持的话） |
| LinkedHashMap     | LinkedHashMultiset     | 是                           |
| ConcurrentHashMap | ConcurrentHashMultiset | 否                           |
| ImmutableMap      | ImmutableMultiset      | 否                           |

**SortedMultiset**

SortedMultiset是Multiset 接口的变种，它支持高效地获取指定范围的子集。比方说，你可以用 latencies.subMultiset(0,BoundType.CLOSED, 100, BoundType.OPEN).size()来统计你的站点中延迟在100毫秒以内的访问，然后把这个值和latencies.size()相比，以获取这个延迟水平在总体访问中的比例。

TreeMultiset实现SortedMultiset接口。在撰写本文档时，ImmutableSortedMultiset还在测试和GWT的兼容性。

> ##### Multimap

每个有经验的Java程序员都在某处实现过Map<K, List<V>>或Map<K, Set<V>>，并且要忍受这个结构的笨拙。例如，Map<K, Set<V>>通常用来表示非标定有向图。Guava的 Multimap可以很容易地把一个键映射到多个值。换句话说，Multimap是把键映射到任意多个值的一般方式。

可以用两种方式思考Multimap的概念：”键-单个值映射”的集合：

a -> 1 a -> 2 a ->4 b -> 3 c -> 5

或者”键-值集合映射”的映射：

a -> [1, 2, 4] b -> 3 c -> 5

一般来说，Multimap接口应该用第一种方式看待，但asMap()视图返回Map<K, Collection<V>>，让你可以按另一种方式看待Multimap。重要的是，不会有任何键映射到空集合：一个键要么至少到一个值，要么根本就不在Multimap中。

很少会直接使用Multimap接口，更多时候你会用ListMultimap或SetMultimap接口，它们分别把键映射到List或Set。

**修改Multimap**

Multimap.get(key)以集合形式返回键所对应的值视图，即使没有任何对应的值，也会返回空集合。ListMultimap.get(key)返回List，SetMultimap.get(key)返回Set。

对值视图集合进行的修改最终都会反映到底层的Multimap。例如：

```
Set<Person> aliceChildren = childrenMultimap.get(alice);
aliceChildren.clear();
aliceChildren.add(bob);
aliceChildren.add(carol);
```

其他（更直接地）修改Multimap的方法有：

| **方法签名**               | **描述**                                                     | **等价于**                                                   |
| -------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| put(K, V)                  | 添加键到单个值的映射                                         | multimap.get(key).add(value)                                 |
| putAll(K, Iterable)        | 依次添加键到多个值的映射                                     | Iterables.addAll(multimap.get(key), values)                  |
| remove(K, V)               | 移除键到值的映射；如果有这样的键值并成功移除，返回true。     | multimap.get(key).remove(value)                              |
| removeAll(K)               | 清除键对应的所有值，返回的集合包含所有之前映射到K的值，但修改这个集合就不会影响Multimap了。 | multimap.get(key).clear()                                    |
| replaceValues(K, Iterable) | 清除键对应的所有值，并重新把key关联到Iterable中的每个元素。返回的集合包含所有之前映射到K的值。 | multimap.get(key).clear(); Iterables.addAll(multimap.get(key), values) |

**Multimap的视图**

Multimap还支持若干强大的视图：

- `asMap`为Multimap<K, V>提供Map<K,Collection<V>>形式的视图。返回的Map支持remove操作，并且会反映到底层的Multimap，但它不支持put或putAll操作。更重要的是，如果你想为Multimap中没有的键返回null，而不是一个新的、可写的空集合，你就可以使用asMap().get(key)。
- `entries`用Collection<Map.Entry<K, V>>返回Multimap中所有”键-单个值映射”——包括重复键。（对SetMultimap，返回的是Set）
- `keySet`用Set表示Multimap中所有不同的键。
- `keys`用Multiset表示Multimap中的所有键，每个键重复出现的次数等于它映射的值的个数。可以从这个Multiset中移除元素，但不能做添加操作；移除操作会反映到底层的Multimap。
- `values()`用一个”扁平”的Collection<V>包含Multimap中的所有值。这有一点类似于Iterables.concat(multimap.asMap().values())，但它直接返回了单个Collection，而不像multimap.asMap().values()那样是按键区分开的Collection。

**Multimap不是Map**

Multimap<K, V>不是Map<K,Collection<V>>，虽然某些Multimap实现中可能使用了map。它们之间的显著区别包括：

- Multimap.get(key)总是返回非null、但是可能空的集合。这并不意味着Multimap为相应的键花费内存创建了集合，而只是提供一个集合视图方便你为键增加映射值——Multimap中已有的集合；如果没有这样的键，返回的空集合也只是持有Multimap引用的栈对象，让你可以用来操作底层的Multimap。因此，返回的集合不会占据太多内存，数据实际上还是存放在Multimap中。*
- 如果你更喜欢像Map那样，为Multimap中没有的键返回null，请使用asMap()视图获取一个Map<K, Collection<V>>。（或者用静态方法Multimaps.asMap()为ListMultimap返回一个Map<K, List<V>>。对于SetMultimap和SortedSetMultimap，也有类似的静态方法存在）
- 当且仅当有值映射到键时，Multimap.containsKey(key)才会返回true。尤其需要注意的是，如果键k之前映射过一个或多个值，但它们都被移除后，Multimap.containsKey(key)会返回false。
- Multimap.entries()返回Multimap中所有”键-单个值映射”——包括重复键。如果你想要得到所有”键-值集合映射”，请使用asMap().entrySet()。
- Multimap.size()返回所有”键-单个值映射”的个数，而非不同键的个数。要得到不同键的个数，请改用Multimap.keySet().size()。

**Multimap的各种实现**

Multimap提供了多种形式的实现。在大多数要使用Map<K, Collection<V>>的地方，你都可以使用它们：

| **实现**                | **键行为类似** | **值行为类似** |
| ----------------------- | -------------- | -------------- |
| ArrayListMultimap       | HashMap        | ArrayList      |
| HashMultimap            | HashMap        | HashSet        |
| LinkedListMultimap*     | LinkedHashMap* | LinkedList*    |
| LinkedHashMultimap*     | LinkedHashMap  | LinkedHashMap  |
| TreeMultimap            | TreeMap        | TreeSet        |
| `ImmutableListMultimap` | ImmutableMap   | ImmutableList  |
| ImmutableSetMultimap    | ImmutableMap   | ImmutableSet   |

除了两个不可变形式的实现，其他所有实现都支持null键和null值

*LinkedListMultimap.entries()保留了所有键和值的迭代顺序。详情见doc链接。

**LinkedHashMultimap保留了映射项的插入顺序，包括键插入的顺序，以及键映射的所有值的插入顺序。

请注意，并非所有的Multimap都和上面列出的一样，使用Map<K, Collection<V>>来实现（特别是，一些Multimap实现用了自定义的hashTable，以最小化开销）

如果你想要更大的定制化，请用[Multimaps.newMultimap(Map, Supplier)或list和 set版本，使用自定义的Collection、List或Set实现Multimap。

> ##### BiMap

传统上，实现键值对的双向映射需要维护两个单独的map，并保持它们间的同步。但这种方式很容易出错，而且对于值已经在map中的情况，会变得非常混乱。例如：

```java
Map<String, Integer> nameToId = Maps.newHashMap();
Map<Integer, String> idToName = Maps.newHashMap();

nameToId.put("Bob", 42);
idToName.put(42, "Bob");
//如果"Bob"和42已经在map中了，会发生什么?
//如果我们忘了同步两个map，会有诡异的bug发生...
```

BiMap是特殊的Map：

- 可以用 inverse()反转BiMap<K, V>的键值映射
- 保证值是唯一的，因此 values()返回Set而不是普通的Collection

在BiMap中，如果你想把键映射到已经存在的值，会抛出IllegalArgumentException异常。如果对特定值，你想要强制替换它的键，请使用 BiMap.forcePut(key, value)。

```java
BiMap<String, Integer> userId = HashBiMap.create();
...

String userForId = userId.inverse().get(id);
```

**BiMap的各种实现**

| **键–**值实现 | **值**–键实现 | **对应的**BiMap**实现** |
| ------------- | ------------- | ----------------------- |
| HashMap       | HashMap       | HashBiMap               |
| ImmutableMap  | ImmutableMap  | ImmutableBiMap          |
| EnumMap       | EnumMap       | EnumBiMap               |
| EnumMap       | HashMap       | EnumHashBiMap           |

注：Maps类中还有一些诸如synchronizedBiMap的BiMap工具方法.

> ##### Table

```java
Table<Vertex, Vertex, Double> weightedGraph = HashBasedTable.create();
weightedGraph.put(v1, v2, 4);
weightedGraph.put(v1, v3, 20);
weightedGraph.put(v2, v3, 5);

weightedGraph.row(v1); // returns a Map mapping v2 to 4, v3 to 20
weightedGraph.column(v3); // returns a Map mapping v1 to 20, v2 to 5
```

通常来说，当你想使用多个键做索引的时候，你可能会用类似Map<FirstName, Map<LastName, Person>>的实现，这种方式很丑陋，使用上也不友好。Guava为此提供了新集合类型Table，它有两个支持所有类型的键：”行”和”列”。Table提供多种视图，以便你从各种角度使用它：

- rowMap()：用Map<R, Map<C, V>>表现Table<R, C, V>。同样的， rowKeySet()返回”行”的集合Set<R>。
- row(r) ：用Map<C, V>返回给定”行”的所有列，对这个map进行的写操作也将写入Table中。
- 类似的列访问方法：columnMap()、columnKeySet()、column(c)。（基于列的访问会比基于的行访问稍微低效点）
- cellSet()：用元素类型为Table.Cell的Set表现Table<R, C, V>。Cell类似于Map.Entry，但它是用行和列两个键区分的。

Table有如下几种实现：

- HashBasedTable：本质上用HashMap<R, HashMap<C, V>>实现；
- TreeBasedTable：本质上用TreeMap<R, TreeMap<C,V>>实现；
- ImmutableTable：本质上用ImmutableMap<R, ImmutableMap<C, V>>实现；注：ImmutableTable对稀疏或密集的数据集都有优化。
- ArrayTable：要求在构造时就指定行和列的大小，本质上由一个二维数组实现，以提升访问速度和密集Table的内存利用率。ArrayTable与其他Table的工作原理有点不同，请参见Javadoc了解详情。

> ##### ClassToInstanceMap

ClassToInstanceMap是一种特殊的Map：它的键是类型，而值是符合键所指类型的对象。

为了扩展Map接口，ClassToInstanceMap额外声明了两个方法：T getInstance(Class) 和T putInstance(Class, T)，从而避免强制类型转换，同时保证了类型安全。

ClassToInstanceMap有唯一的泛型参数，通常称为B，代表Map支持的所有类型的上界。例如：

```
ClassToInstanceMap<Number> numberDefaults=MutableClassToInstanceMap.create();
numberDefaults.putInstance(Integer.class, Integer.valueOf(0));
```

从技术上讲，ClassToInstanceMap<B>实现了Map<Class<? extends B>, B>——或者换句话说，是一个映射B的子类型到对应实例的Map。这让ClassToInstanceMap包含的泛型声明有点令人困惑，但请记住B始终是Map所支持类型的上界——通常B就是Object。

对于ClassToInstanceMap，Guava提供了两种有用的实现：MutableClassToInstanceMap和 ImmutableClassToInstanceMap。

> ##### RangeSet

RangeSet描述了一组不相连的、非空的区间。当把一个区间添加到可变的RangeSet时，所有相连的区间会被合并，空区间会被忽略。例如：

```java
RangeSet<Integer> rangeSet = TreeRangeSet.create();
rangeSet.add(Range.closed(1, 10)); // {[1,10]}
rangeSet.add(Range.closedOpen(11, 15));//不相连区间:{[1,10], [11,15)}
rangeSet.add(Range.closedOpen(15, 20)); //相连区间; {[1,10], [11,20)}
rangeSet.add(Range.openClosed(0, 0)); //空区间; {[1,10], [11,20)}
rangeSet.remove(Range.open(5, 10)); //分割[1, 10]; {[1,5], [10,10], [11,20)}
```

请注意，要合并Range.closed(1, 10)和Range.closedOpen(11, 15)这样的区间，你需要首先用Range.canonical(DiscreteDomain)对区间进行预处理，例如DiscreteDomain.integers()。

注：RangeSet不支持GWT，也不支持JDK5和更早版本；因为，RangeSet需要充分利用JDK6中NavigableMap的特性。

**RangeSet的视图**

RangeSet的实现支持非常广泛的视图：

- complement()：返回RangeSet的补集视图。complement也是RangeSet类型,包含了不相连的、非空的区间。
- subRangeSet(Range<C>)：返回RangeSet与给定Range的交集视图。这扩展了传统排序集合中的headSet、subSet和tailSet操作。
- asRanges()：用Set<Range<C>>表现RangeSet，这样可以遍历其中的Range。
- asSet(DiscreteDomain<C>)（仅ImmutableRangeSet支持）：用ImmutableSortedSet<C>表现RangeSet，以区间中所有元素的形式而不是区间本身的形式查看。（这个操作不支持DiscreteDomain 和RangeSet都没有上边界，或都没有下边界的情况）

**RangeSet的查询方法**

为了方便操作，RangeSet直接提供了若干查询方法，其中最突出的有:

- contains(C)：RangeSet最基本的操作，判断RangeSet中是否有任何区间包含给定元素。
- rangeContaining(C)：返回包含给定元素的区间；若没有这样的区间，则返回null。
- encloses(Range<C>)：简单明了，判断RangeSet中是否有任何区间包括给定区间。
- span()：返回包括RangeSet中所有区间的最小区间。

> ##### RangeMap

RangeMap描述了”不相交的、非空的区间”到特定值的映射。和RangeSet不同，RangeMap不会合并相邻的映射，即便相邻的区间映射到相同的值。例如：

```java
RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
rangeMap.put(Range.closed(1, 10), "foo"); //{[1,10] => "foo"}
rangeMap.put(Range.open(3, 6), "bar"); //{[1,3] => "foo", (3,6) => "bar", [6,10] => "foo"}
rangeMap.put(Range.open(10, 20), "foo"); //{[1,3] => "foo", (3,6) => "bar", [6,10] => "foo", (10,20) => "foo"}
rangeMap.remove(Range.closed(5, 11)); //{[1,3] => "foo", (3,5) => "bar", (11,20) => "foo"}
```

**RangeMap的视图**

RangeMap提供两个视图：

- asMapOfRanges()：用Map<Range<K>, V>表现RangeMap。这可以用来遍历RangeMap。
- subRangeMap(Range<K>)：用RangeMap类型返回RangeMap与给定Range的交集视图。这扩展了传统的headMap、subMap和tailMap操作。

#### 2.3 强大的集合工具类: 

提供java.util.Collections中没有的集合工具（此处较为繁琐）

任何对JDK集合框架有经验的程序员都熟悉和喜欢`java.util.Collections`包含的工具方法。Guava沿着这些路线提供了更多的工具方法：适用于所有集合的静态方法。这是Guava最流行和成熟的部分之一。

我们用相对直观的方式把工具类与特定集合接口的对应关系归纳如下：

| **集合接口** | **属于**JDK**还是**Guava | **对应的**Guava**工具类**                       |
| ------------ | ------------------------ | ----------------------------------------------- |
| Collection   | JDK                      | `Collections2`：不要和java.util.Collections混淆 |
| List         | JDK                      | `Lists`                                         |
| Set          | JDK                      | `Sets`                                          |
| SortedSet    | JDK                      | `Sets`                                          |
| Map          | JDK                      | `Maps`                                          |
| SortedMap    | JDK                      | `Maps`                                          |
| Queue        | JDK                      | `Queues`                                        |
| Multiset     | Guava                    | `Multisets`                                     |
| Multimap     | Guava                    | `Multimaps`                                     |
| BiMap        | Guava                    | `Maps`                                          |
| Table        | Guava                    | `Tables`                                        |

> ##### 静态工厂方法

在JDK 7之前，构造新的范型集合时要讨厌地重复声明范型：

```java
List<TypeThatsTooLongForItsOwnGood> list = new ArrayList<TypeThatsTooLongForItsOwnGood>();
```

我想我们都认为这很讨厌。因此Guava提供了能够推断范型的静态工厂方法：

```java
List<TypeThatsTooLongForItsOwnGood> list = Lists.newArrayList();
Map<KeyType, LongishValueType> map = Maps.newLinkedHashMap();
```

可以肯定的是，JDK7版本的钻石操作符(<>)没有这样的麻烦：

```java
List<TypeThatsTooLongForItsOwnGood> list = new ArrayList<>();
```

但Guava的静态工厂方法远不止这么简单。用工厂方法模式，我们可以方便地在初始化时就指定起始元素。

```java
Set<Type> copySet = Sets.newHashSet(elements);
List<String> theseElements = Lists.newArrayList("alpha", "beta", "gamma");
```

此外，通过为工厂方法命名（Effective Java第一条），我们可以提高集合初始化大小的可读性：

```java
List<Type> exactly100 = Lists.newArrayListWithCapacity(100);
List<Type> approx100 = Lists.newArrayListWithExpectedSize(100);
Set<Type> approx100Set = Sets.newHashSetWithExpectedSize(100);
```

确切的静态工厂方法和相应的工具类一起罗列在下面的章节。

注意：Guava引入的新集合类型没有暴露原始构造器，也没有在工具类中提供初始化方法。而是直接在集合类中提供了静态工厂方法，例如：

```java
Multiset<String> multiset = HashMultiset.create();
```

> ##### Iterables

在可能的情况下，Guava提供的工具方法更偏向于接受Iterable而不是Collection类型。在Google，对于不存放在主存的集合——比如从数据库或其他数据中心收集的结果集，因为实际上还没有攫取全部数据，这类结果集都不能支持类似size()的操作 ——通常都不会用Collection类型来表示。

因此，很多你期望的支持所有集合的操作都在`Iterables`类中。大多数Iterables方法有一个在Iterators类中的对应版本，用来处理Iterator。

截至Guava 1.2版本，Iterables使用`FluentIterable类`进行了补充，它包装了一个Iterable实例，并对许多操作提供了”fluent”（链式调用）语法。

下面列出了一些最常用的工具方法。

**常规方法**

| `concat(Iterable<Iterable>)`        | 串联多个iterables的懒视图*                                   | `concat(Iterable...)`                                        |
| ----------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| `frequency(Iterable, Object)`       | 返回对象在iterable中出现的次数                               | 与Collections.frequency (Collection,  Object)比较；``Multiset |
| `partition(Iterable, int)`          | 把iterable按指定大小分割，得到的子集都不能进行修改操作       | `Lists.partition(List, int)`；`paddedPartition(Iterable, int)` |
| `getFirst(Iterable, T default)`     | 返回iterable的第一个元素，若iterable为空则返回默认值         | 与Iterable.iterator(). next()比较;`FluentIterable.first()`   |
| `getLast(Iterable)`                 | 返回iterable的最后一个元素，若iterable为空则抛出NoSuchElementException | `getLast(Iterable, T default)`； `FluentIterable.last()`     |
| `elementsEqual(Iterable, Iterable)` | 如果两个iterable中的所有元素相等且顺序一致，返回true         | 与List.equals(Object)比较                                    |
| `unmodifiableIterable(Iterable)`    | 返回iterable的不可变视图                                     | 与Collections. unmodifiableCollection(Collection)比较        |
| `limit(Iterable, int)`              | 限制iterable的元素个数限制给定值                             | `FluentIterable.limit(int)`                                  |
| `getOnlyElement(Iterable)`          | 获取iterable中唯一的元素，如果iterable为空或有多个元素，则快速失败 | `getOnlyElement(Iterable, T default)`                        |



```java
Iterable<Integer> concatenated = Iterables.concat(
        Ints.asList(1, 2, 3),
        Ints.asList(4, 5, 6)); // concatenated包括元素 1, 2, 3, 4, 5, 6
String lastAdded = Iterables.getLast(myLinkedHashSet);
String theElement = Iterables.getOnlyElement(thisSetIsDefinitelyASingleton);
//如果set不是单元素集，就会出错了！
```

**与Collection方法相似的工具方法**

通常来说，Collection的实现天然支持操作其他Collection，但却不能操作Iterable。

下面的方法中，如果传入的Iterable是一个Collection实例，则实际操作将会委托给相应的Collection接口方法。例如，往Iterables.size方法传入是一个Collection实例，它不会真的遍历iterator获取大小，而是直接调用Collection.size。

| **方法**                                               | **类似的****Collection****方法** | **等价的****FluentIterable****方法** |
| ------------------------------------------------------ | -------------------------------- | ------------------------------------ |
| `addAll(Collection addTo,  Iterable toAdd)`            | Collection.addAll(Collection)    |                                      |
| `contains(Iterable, Object)`                           | Collection.contains(Object)      | `FluentIterable.contains(Object)`    |
| `removeAll(Iterable  removeFrom, Collection toRemove)` | Collection.removeAll(Collection) |                                      |
| `retainAll(Iterable  removeFrom, Collection toRetain)` | Collection.retainAll(Collection) |                                      |
| `size(Iterable)`                                       | Collection.size()                | `FluentIterable.size()`              |
| `toArray(Iterable, Class)`                             | Collection.toArray(T[])          | `FluentIterable.toArray(Class)`      |
| `isEmpty(Iterable)`                                    | Collection.isEmpty()             | `FluentIterable.isEmpty()`           |
| `get(Iterable, int)`                                   | List.get(int)                    | `FluentIterable.get(int)`            |
| `toString(Iterable)`                                   | Collection.toString()            | `FluentIterable.toString()`          |

**FluentIterable**

除了上面和第四章提到的方法，FluentIterable还有一些便利方法用来把自己拷贝到不可变集合

| ImmutableList      |                                    |
| ------------------ | ---------------------------------- |
| ImmutableSet       | `toImmutableSet()`                 |
| ImmutableSortedSet | `toImmutableSortedSet(Comparator)` |

> ##### Lists

除了静态工厂方法和函数式编程方法，`Lists`为List类型的对象提供了若干工具方法。

| **方法**               | **描述**                                                     |
| ---------------------- | ------------------------------------------------------------ |
| `partition(List, int)` | 把List按指定大小分割                                         |
| `reverse(List)`        | 返回给定List的反转视图。注: 如果List是不可变的，考虑改用`ImmutableList.reverse()`。 |

```java
List countUp = Ints.asList(1, 2, 3, 4, 5);
List countDown = Lists.reverse(theList); // {5, 4, 3, 2, 1}
List<List> parts = Lists.partition(countUp, 2);//{{1,2}, {3,4}, {5}}
```

**静态工厂方法**

Lists提供如下静态工厂方法：

| **具体实现类型** | **工厂方法**                                                 |
| ---------------- | ------------------------------------------------------------ |
| ArrayList        | basic, with elements, from `Iterable`, with exact capacity, with expected size, from `Iterator` |
| LinkedList       | basic, from `Iterable`                                       |

> ##### Sets

`Sets`工具类包含了若干好用的方法。

**集合理论方法**

我们提供了很多标准的集合运算（Set-Theoretic）方法，这些方法接受Set参数并返回`SetView`，可用于：

- 直接当作Set使用，因为SetView也实现了Set接口；
- 用`copyInto(Set)`拷贝进另一个可变集合；
- 用`immutableCopy()`对自己做不可变拷贝。

| **方法**                         |
| -------------------------------- |
| `union(Set, Set)`                |
| `intersection(Set, Set)`         |
| `difference(Set, Set)`           |
| `symmetricDifference(Set,  Set)` |

使用范例：

```java
Set<String> wordsWithPrimeLength = ImmutableSet.of("one", "two", "three", "six", "seven", "eight");
Set<String> primes = ImmutableSet.of("two", "three", "five", "seven");
SetView<String> intersection = Sets.intersection(primes,wordsWithPrimeLength);
// intersection包含"two", "three", "seven"
return intersection.immutableCopy();//可以使用交集，但不可变拷贝的读取效率更高
```

**其他Set工具方法**

| **方法**                      | **描述**               | **另请参见**               |
| ----------------------------- | ---------------------- | -------------------------- |
| `cartesianProduct(List<Set>)` | 返回所有集合的笛卡儿积 | `cartesianProduct(Set...)` |
| `powerSet(Set)`               | 返回给定集合的所有子集 |                            |

```java
Set<String> animals = ImmutableSet.of("gerbil", "hamster");
Set<String> fruits = ImmutableSet.of("apple", "orange", "banana");
 
Set<List<String>> product = Sets.cartesianProduct(animals, fruits);
// {{"gerbil", "apple"}, {"gerbil", "orange"}, {"gerbil", "banana"},
//  {"hamster", "apple"}, {"hamster", "orange"}, {"hamster", "banana"}}
 
Set<Set<String>> animalSets = Sets.powerSet(animals);
// {{}, {"gerbil"}, {"hamster"}, {"gerbil", "hamster"}}
```

**静态工厂方法**

Sets提供如下静态工厂方法：

| **具体实现类型** | **工厂方法**                                                 |
| ---------------- | ------------------------------------------------------------ |
| HashSet          | basic, with elements, from `Iterable`, with expected size, from `Iterator` |
| LinkedHashSet    | basic, from `Iterable`, with expected size                   |
| TreeSet          | basic, with `Comparator`, from `Iterable`                    |

> ##### Maps

`Maps`类有若干值得单独说明的、很酷的方法。

**uniqueIndex**

`Maps.uniqueIndex(Iterable,Function)`通常针对的场景是：有一组对象，它们在某个属性上分别有独一无二的值，而我们希望能够按照这个属性值查找对象。

比方说，我们有一堆字符串，这些字符串的长度都是独一无二的，而我们希望能够按照特定长度查找字符串：

```java
ImmutableMap<Integer, String> stringsByIndex = Maps.uniqueIndex(strings,
    new Function<String, Integer> () {
        public Integer apply(String string) {
            return string.length();
        }
    });
```

如果索引值不是独一无二的，请参见下面的Multimaps.index方法。

**difference**

`Maps.difference(Map, Map)`用来比较两个Map以获取所有不同点。该方法返回MapDifference对象，把不同点的维恩图分解为：

| `entriesInCommon()`    | 两个Map中都有的映射项，包括匹配的键与值                      |
| ---------------------- | ------------------------------------------------------------ |
| `entriesDiffering()`   | 键相同但是值不同值映射项。返回的Map的值类型为`MapDifference.ValueDifference`，以表示左右两个不同的值 |
| `entriesOnlyOnLeft()`  | 键只存在于左边Map的映射项                                    |
| `entriesOnlyOnRight()` | 键只存在于右边Map的映射项                                    |

```java
Map<String, Integer> left = ImmutableMap.of("a", 1, "b", 2, "c", 3);
Map<String, Integer> left = ImmutableMap.of("a", 1, "b", 2, "c", 3);
MapDifference<String, Integer> diff = Maps.difference(left, right);
 
diff.entriesInCommon(); // {"b" => 2}
diff.entriesInCommon(); // {"b" => 2}
diff.entriesOnlyOnLeft(); // {"a" => 1}
diff.entriesOnlyOnRight(); // {"d" => 5}
```

**处理BiMap的工具方法**

Guava中处理BiMap的工具方法在Maps类中，因为BiMap也是一种Map实现。

| **BiMap****工具方法**      | **相应的****Map****工具方法**    |
| -------------------------- | -------------------------------- |
| `synchronizedBiMap(BiMap)` | Collections.synchronizedMap(Map) |
| `unmodifiableBiMap(BiMap)` | Collections.unmodifiableMap(Map) |

**静态工厂方法**

Maps提供如下静态工厂方法：

| **具体实现类型**            | **工厂方法**                               |
| --------------------------- | ------------------------------------------ |
| HashMap                     | basic, from `Map`, with expected size      |
| LinkedHashMap               | basic, from `Map`                          |
| TreeMap                     | basic, from `Comparator`, from `SortedMap` |
| EnumMap                     | from `Class`, from `Map`                   |
| ConcurrentMap：支持所有操作 | basic                                      |
| IdentityHashMap             | basic                                      |

> ##### Multisets

标准的Collection操作会忽略Multiset重复元素的个数，而只关心元素是否存在于Multiset中，如containsAll方法。为此，`Multisets`提供了若干方法，以顾及Multiset元素的重复性：

| **方法**                                                     | **说明**                                                     | **和****Collection****方法的区别**                           |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| `containsOccurrences(Multiset  sup, Multiset sub)`           | 对任意o，如果sub.count(o)<=super.count(o)，返回true          | Collection.containsAll忽略个数，而只关心sub的元素是否都在super中 |
| `removeOccurrences(Multiset  removeFrom, Multiset toRemove)` | 对toRemove中的重复元素，仅在removeFrom中删除相同个数。       | Collection.removeAll移除所有出现在toRemove的元素             |
| `retainOccurrences(Multiset  removeFrom, Multiset toRetain)` | 修改removeFrom，以保证任意o都符合removeFrom.count(o)<=toRetain.count(o) | Collection.retainAll保留所有出现在toRetain的元素             |
| `intersection(Multiset,  Multiset)`                          | 返回两个multiset的交集;                                      | 没有类似方法                                                 |

```java
Multiset<String> multiset1 = HashMultiset.create();
multiset1.add("a", 2);
 
Multiset<String> multiset2 = HashMultiset.create();
multiset2.add("a", 5);
 
multiset1.containsAll(multiset2); //返回true；因为包含了所有不重复元素，
//虽然multiset1实际上包含2个"a"，而multiset2包含5个"a"
Multisets.containsOccurrences(multiset1, multiset2); // returns false
 
multiset2.removeOccurrences(multiset1); // multiset2 现在包含3个"a"
multiset2.removeAll(multiset1);//multiset2移除所有"a"，虽然multiset1只有2个"a"
multiset2.isEmpty(); // returns true
```

Multisets中的其他工具方法还包括：

| `copyHighestCountFirst(Multiset)`            | 返回Multiset的不可变拷贝，并将元素按重复出现的次数做降序排列 |
| -------------------------------------------- | ------------------------------------------------------------ |
| `unmodifiableMultiset(Multiset)`             | 返回Multiset的只读视图                                       |
| `unmodifiableSortedMultiset(SortedMultiset)` | 返回SortedMultiset的只读视图                                 |

```java
Multiset<String> multiset = HashMultiset.create();
multiset.add("a", 3);
multiset.add("b", 5);
multiset.add("c", 1);
 
ImmutableMultiset highestCountFirst = Multisets.copyHighestCountFirst(multiset);
//highestCountFirst，包括它的entrySet和elementSet，按{"b", "a", "c"}排列元素
```

> ##### Multimaps

`Multimaps`提供了若干值得单独说明的通用工具方法

**index**

作为Maps.uniqueIndex的兄弟方法，`Multimaps.index(Iterable, Function)`通常针对的场景是：有一组对象，它们有共同的特定属性，我们希望按照这个属性的值查询对象，但属性值不一定是独一无二的。

比方说，我们想把字符串按长度分组。

```java
ImmutableSet digits = ImmutableSet.of("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine");
Function<String, Integer> lengthFunction = new Function<String, Integer>() {
    public Integer apply(String string) {
        return string.length();
    }
};
 
ImmutableListMultimap<Integer, String> digitsByLength= Multimaps.index(digits, lengthFunction);
/*
*  digitsByLength maps:
*  3 => {"one", "two", "six"}
*  4 => {"zero", "four", "five", "nine"}
*  5 => {"three", "seven", "eight"}
*/
```

**invertFrom**

鉴于Multimap可以把多个键映射到同一个值，也可以把一个键映射到多个值，反转Multimap也会很有用。Guava 提供了`invertFrom(Multimap toInvert,Multimap dest)`做这个操作，并且你可以自由选择反转后的Multimap实现。

注：如果你使用的是ImmutableMultimap，考虑改用`ImmutableMultimap.inverse()`做反转。

```java
ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
multimap.putAll("b", Ints.asList(2, 4, 6));
multimap.putAll("a", Ints.asList(4, 2, 1));
multimap.putAll("c", Ints.asList(2, 5, 3));
 
TreeMultimap<Integer, String> inverse = Multimaps.invertFrom(multimap, TreeMultimap<String, Integer>.create());
//注意我们选择的实现，因为选了TreeMultimap，得到的反转结果是有序的
/*
* inverse maps:
*  1 => {"a"}
*  2 => {"a", "b", "c"}
*  3 => {"c"}
*  4 => {"a", "b"}
*  5 => {"c"}
*  6 => {"b"}
*/
```

**forMap**

想在Map对象上使用Multimap的方法吗？`forMap(Map)`把Map包装成SetMultimap。这个方法特别有用，例如，与Multimaps.invertFrom结合使用，可以把多对一的Map反转为一对多的Multimap。

```java
Map<String, Integer> map = ImmutableMap.of("a", 1, "b", 1, "c", 2);
SetMultimap<String, Integer> multimap = Multimaps.forMap(map);
// multimap：["a" => {1}, "b" => {1}, "c" => {2}]
Multimap<Integer, String> inverse = Multimaps.invertFrom(multimap, HashMultimap<Integer, String>.create());
// inverse：[1 => {"a","b"}, 2 => {"c"}]
```

**包装器**

Multimaps提供了传统的包装方法，以及让你选择Map和Collection类型以自定义Multimap实现的工具方法。

| 只读包装   | `Multimap` | `ListMultimap` | `SetMultimap` | `SortedSetMultimap` |
| ---------- | ---------- | -------------- | ------------- | ------------------- |
| 同步包装   | `Multimap` | `ListMultimap` | `SetMultimap` | `SortedSetMultimap` |
| 自定义实现 | `Multimap` | `ListMultimap` | `SetMultimap` | `SortedSetMultimap` |

自定义Multimap的方法允许你指定Multimap中的特定实现。但要注意的是：

- Multimap假设对Map和Supplier产生的集合对象有完全所有权。这些自定义对象应避免手动更新，并且在提供给Multimap时应该是空的，此外还不应该使用软引用、弱引用或虚引用。
- 无法保证修改了Multimap以后，底层Map的内容是什么样的。
- 即使Map和Supplier产生的集合都是线程安全的，它们组成的Multimap也不能保证并发操作的线程安全性。并发读操作是工作正常的，但需要保证并发读写的话，请考虑用同步包装器解决。
- 只有当Map、Supplier、Supplier产生的集合对象、以及Multimap存放的键值类型都是可序列化的，Multimap才是可序列化的。
- Multimap.get(key)返回的集合对象和Supplier返回的集合对象并不是同一类型。但如果Supplier返回的是随机访问集合，那么Multimap.get(key)返回的集合也是可随机访问的。

请注意，用来自定义Multimap的方法需要一个Supplier参数，以创建崭新的集合。下面有个实现ListMultimap的例子——用TreeMap做映射，而每个键对应的多个值用LinkedList存储。

```java
ListMultimap<String, Integer> myMultimap = Multimaps.newListMultimap(
    Maps.<String, Collection>newTreeMap(),
    new Supplier<LinkedList>() {
        public LinkedList get() {
            return Lists.newLinkedList();
        }
    });
```

> ##### Tables

`Tables`类提供了若干称手的工具方法。

**自定义Table**

堪比Multimaps.newXXXMultimap(Map, Supplier)工具方法，`Tables.newCustomTable(Map, Supplier<Map>)`允许你指定Table用什么样的map实现行和列。

```java
// 使用LinkedHashMaps替代HashMaps
Table<String, Character, Integer> table = Tables.newCustomTable(
Maps.<String, Map<Character, Integer>>newLinkedHashMap(),
new Supplier<Map<Character, Integer>> () {
public Map<Character, Integer> get() {
return Maps.newLinkedHashMap();
}
});
```

**transpose**

`transpose(Table<R, C, V>)`方法允许你把Table<C, R, V>转置成Table<R, C, V>。例如，如果你在用Table构建加权有向图，这个方法就可以把有向图反转。

**包装器**

还有很多你熟悉和喜欢的Table包装类。然而，在大多数情况下还请使用`ImmutableTable`

#### 2.4 扩展工具类：

让实现和扩展集合类变得更容易，比如创建`Collection`的装饰器，或实现迭代器

**简单使用**

不提供。guava本生就是JDK拓展，自己再去拓展,,,

## 3. 缓存[Caches]

Guava Cache：本地缓存实现，支持多种缓存过期策略

#### 3.1适用性

缓存在很多场景下都是相当有用的。例如，计算或检索一个值的代价很高，并且对同样的输入需要不止一次获取值的时候，就应当考虑使用缓存。

`Guava Cache与ConcurrentMap很相似，但也不完全一样。最基本的区别是ConcurrentMap会一直保存所有添加的元素，直到显式地移除。相对地，Guava Cache为了限制内存占用，通常都设定为自动回收元素。在某些场景下，尽管LoadingCache 不回收元素，它也是很有用的，因为它会自动加载缓存。`

`通常来说，`Guava Cache适用于：

- 你愿意消耗一些内存空间来提升速度。
- 你预料到某些键会被查询一次以上。
- 缓存中存放的数据总量不会超出内存容量。（Guava Cache是单个应用运行时的本地缓存。它不把数据存放到文件或外部服务器。如果这不符合你的需求，请尝试Memcached这类工具）

如果你的场景符合上述的每一条，Guava Cache就适合你。

```
如同范例代码展示的一样，Cache实例通过CacheBuilder生成器模式获取，但是自定义你的缓存才是最有趣的部分。
*注*：如果你不需要Cache中的特性，使用ConcurrentHashMap有更好的内存效率——但Cache的大多数特性都很难基于旧有的ConcurrentMap复制，甚至根本不可能做到。
```

#### 3.2缓存使用

> ##### 通过CacheBuilder类构建一个缓存对象

CacheBuilder类采用builder设计模式，它的每个方法都返回CacheBuilder本身，直到build方法被调用。构建一个缓存对象代码如下。

```java
Cache<String,String> cache = CacheBuilder.newBuilder().build();
cache.put("word","Hello Guava Cache");
System.out.println(cache.getIfPresent("word"));
```

上面的代码通过**CacheBuilder.newBuilder().build()**这句代码创建了一个Cache缓存对象，并在缓存对象中存储了*key*为word，*value*为Hello Guava Cache的一条记录。可以看到Cache非常类似于JDK中的Map，但是相比于Map，Guava Cache提供了很多更强大的功能。

从LoadingCache查询的正规方式是使用`get(K)`方法。这个方法要么返回已经缓存的值，要么使用CacheLoader向缓存原子地加载新值（通过`load(String key)` 方法加载）。由于CacheLoader可能抛出异常，`LoadingCache.get(K)`也声明抛出ExecutionException异常。如果你定义的CacheLoader没有声明任何检查型异常，则可以通过`getUnchecked(K)`查找缓存；但必须注意，一旦CacheLoader声明了检查型异常，就不可以调用`getUnchecked(K)`。

```java
LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, String>() {
                    public String load(String key) throws RuntimeException {
                        Random random = new Random();
                        return key + random.nextInt(100);
                    }
                });
        try {
            String v = cache.get("key");
            System.out.println(v);
        } catch (RuntimeException | ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
```

> ##### 设置最大存储

Guava Cache可以在构建缓存对象时指定缓存所能够存储的最大记录数量。当Cache中的记录数量达到最大值后再调用put方法向其中添加对象，Guava会先从当前缓存的对象记录中选择一条删除掉，腾出空间后再将新的对象存储到Cache中。

```java
Cache<String, String> cache = CacheBuilder.newBuilder()  
        .maximumSize(2)                                  
        .build();                                        
cache.put("key1", "value1");                             
cache.put("key2", "value2");                             
cache.put("key3", "value3");                             
System.out.println("第一个值：" + cache.getIfPresent("key1"));
System.out.println("第二个值：" + cache.getIfPresent("key2"));
System.out.println("第三个值：" + cache.getIfPresent("key3"));
```

> ##### 设置过期时间

在构建Cache对象时，可以通过CacheBuilder类的expireAfterAccess和expireAfterWrite两个方法为缓存中的对象指定过期时间，**使用`CacheBuilder`构建的缓存不会“自动”执行清理和逐出值，也不会在值到期后立即执行或逐出任何类型。相反，它在写入操作期间执行少量维护，或者在写入很少的情况下偶尔执行读取操作。**其中，expireAfterWrite方法指定对象被写入到缓存后多久过期，expireAfterAccess指定对象多久没有被访问后过期。

```java
Cache<String, String> cache = CacheBuilder.newBuilder()                                   
        .maximumSize(2)                                                                   
        .expireAfterWrite(3, TimeUnit.SECONDS)                                           
        .build();                                                                         
cache.put("key1", "value1");                                                             
int time = 1;                                                                             
while (true) {                                                                           
    System.out.println("第" + time++ + "次取到key1的值为：" + cache.getIfPresent("key1"));   
    Thread.sleep(1000);                                                                   
    if (time > 5) {                                                                       
        break;                                                                           
    }                                                                                     
}                                                                                         
cache = CacheBuilder.newBuilder()                                                         
        .maximumSize(2)                                                                   
        .expireAfterAccess(3, TimeUnit.SECONDS)                                           
        .build();                                                                         
cache.put("key1", "value1");                                                             
time = 1;                                                                                 
while (true) {                                                                           
    Thread.sleep(time * 1000);                                                           
    System.out.println("睡眠" + time++ + "秒后取到key1的值为：" + cache.getIfPresent("key1"));         
    if (time > 5) {                                                                       
        break;                                                                           
    }                                                                                     
}                                                                                            
```

> ##### 弱引用

可以通过weakKeys和weakValues方法指定Cache只保存对缓存记录key和value的弱引用。这样当没有其他强引用指向key和value时，key和value对象就会被垃圾回收器回收。

```java
Cache<String, Object> cache = CacheBuilder.newBuilder()  
        .maximumSize(2)                                  
        .weakValues()                                    
        .build();                                        
Object value = new Object();                             
cache.put("key1", value);                                
                                                         
value = new Object();//原对象不再有强引用                         
System.gc();                                             
System.out.println(cache.getIfPresent("key1"));          
```

上面代码的打印结果是null。构建Cache时通过weakValues方法指定Cache只保存记录值的一个弱引用。当给value引用赋值一个新的对象之后，就不再有任何一个强引用指向原对象。System.gc()触发垃圾回收后，原对象就被清除了。

> ##### 显式清除

可以调用Cache的invalidateAll或invalidate方法显示删除Cache中的记录。invalidate方法一次只能删除Cache中一个记录，接收的参数是要删除记录的key。invalidateAll方法可以批量删除Cache中的记录，当没有传任何参数时，invalidateAll方法将清除Cache中的全部记录。invalidateAll也可以接收一个Iterable类型的参数，参数中包含要删除记录的所有key值。下面代码对此做了示例。

```java
Cache<String, String> cache = CacheBuilder.newBuilder().build();  
Object value = new Object();                                      
cache.put("key1", "value1");                                      
cache.put("key2", "value2");                                      
cache.put("key3", "value3");                                      
                                                                  
List<String> list = new ArrayList<String>();                      
list.add("key1");                                                 
list.add("key2");                                                 
                                                                  
cache.invalidateAll(list);//批量清除list中全部key对应的记录                   
System.out.println(cache.getIfPresent("key1"));                   
System.out.println(cache.getIfPresent("key2"));                   
System.out.println(cache.getIfPresent("key3"));                   
```

代码中构造了一个集合list用于保存要删除记录的key值，然后调用invalidateAll方法批量删除key1和key2对应的记录，只剩下key3对应的记录没有被删除。

> ##### 移除监听器

可以为Cache对象添加一个移除监听器，这样当有记录被删除时可以感知到这个事件。

```java
        RemovalListener<String, String> listener = new RemovalListener<String, String>() {
            public void onRemoval(RemovalNotification<String, String> notification) {
                System.out.println("[" + notification.getKey() + ":" + notification.getValue() + "] is removed!");
            }
        };
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(3)
                .removalListener(listener)
                .build();
        Object value = new Object();
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value3");
        cache.put("key5", "value3");
        cache.put("key6", "value3");
        cache.put("key7", "value3");
        cache.put("key8", "value3");
```

> ##### 自动加载

Cache的get方法有两个参数，第一个参数是要从Cache中获取记录的key，第二个记录是一个Callable对象。当缓存中已经存在key对应的记录时，get方法直接返回key对应的记录。如果缓存中不包含key对应的记录，Guava会启动一个线程执行Callable对象中的call方法，call方法的返回值会作为key对应的值被存储到缓存中，并且被get方法返回。下面是一个多线程的例子：

```java
Cache<String, String> cache = CacheBuilder.newBuilder()
        .maximumSize(3)
        .build();

new Thread(new Runnable() {
    public void run() {
        System.out.println("thread1");
        try {
            String value = cache.get("key", new Callable<String>() {
                public String call() throws Exception {
                    System.out.println("load1"); //加载数据线程执行标志
                    Thread.sleep(1000); //模拟加载时间
                    return "auto load by Callable";
                }
            });
            System.out.println("thread1 " + value);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}).start();

new Thread(new Runnable() {
    public void run() {
        System.out.println("thread2");
        try {
            String value = cache.get("key", new Callable<String>() {
                public String call() throws Exception {
                    System.out.println("load2"); //加载数据线程执行标志
                    Thread.sleep(1000); //模拟加载时间
                    return "auto load by Callable";
                }
            });
            System.out.println("thread2 " + value);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}).start();

Thread.sleep(3000);
```

从结果中可以看出，虽然是两个线程同时调用get方法，但只有一个get方法中的Callable会被执行(没有打印出load2)。Guava可以保证当有多个线程同时访问Cache中的一个key时，如果key对应的记录不存在，Guava只会启动一个线程执行get方法中Callable参数对应的任务加载数据存到缓存。当加载完数据后，任何线程中的get方法都会获取到key对应的值。

## 4. 函数式风格[Functional idioms]

Guava的函数式支持可以显著简化代码，但请谨慎使用它(不要滥用)

截至JDK7，Java中也只能通过笨拙冗长的匿名类来达到近似函数式编程的效果。预计JDK8中会有所改变，但Guava现在就想给JDK5以上用户提供这类支持。

过度使用Guava函数式编程会导致冗长、混乱、可读性差而且低效的代码。这是迄今为止最容易（也是最经常）被滥用的部分，如果你想通过函数式风格达成一行代码，致使这行代码长到荒唐，Guava团队会泪流满面。

比较如下代码：

```java
Function<String, Integer> lengthFunction = new Function<String, Integer>() {
    public Integer apply(String string) {
        return string.length();
    }
};
Predicate<String> allCaps = new Predicate<String>() {
    public boolean apply(String string) {
        return CharMatcher.JAVA_UPPER_CASE.matchesAllOf(string);
    }
};
Multiset<Integer> lengths = HashMultiset.create(
     Iterables.transform(Iterables.filter(strings, allCaps), lengthFunction));
```

FluentIterable的版本

```java
Multiset<Integer> lengths = HashMultiset.create(
    FluentIterable.from(strings)
        .filter(new Predicate<String>() {
            public boolean apply(String string) {
                return CharMatcher.JAVA_UPPER_CASE.matchesAllOf(string);
            }
        })
        .transform(new Function<String, Integer>() {
            public Integer apply(String string) {
                return string.length();
            }
        }));
```

还有

```java
Multiset<Integer> lengths = HashMultiset.create();
for (String string : strings) {
    if (CharMatcher.JAVA_UPPER_CASE.matchesAllOf(string)) {
        lengths.add(string.length());
    }
}
```

截至JDK7，命令式代码仍应是默认和第一选择。不应该随便使用函数式风格，除非你绝对确定以下两点之一：

- 使用函数式风格以后，整个工程的代码行会净减少。在上面的例子中，函数式版本用了11行， 命令式代码用了6行，把函数的定义放到另一个文件或常量中，并不能帮助减少总代码行。
- 为了提高效率，转换集合的结果需要懒视图，而不是明确计算过的集合。

#### Functions[函数]和Predicates[断言]

Guava提供两个基本的函数式接口：

- Function<A, B>，它声明了单个方法B apply(A input)。Function对象通常被预期为引用透明的——没有副作用——并且引用透明性中的”相等”语义与equals一致，如a.equals(b)意味着function.apply(a).equals(function.apply(b))。
- Predicate<T>，它声明了单个方法boolean apply(T input)。Predicate对象通常也被预期为无副作用函数，并且”相等”语义与equals一致。

## 5. 并发[Concurrency]

强大而简单的抽象，让编写正确的并发代码更简单

#### 5.1ThreadFactoryBuilder

ThreadFactoryBuilder是一个Builder设计模式的应用,可以设置守护进程、错误处理器、线程名字。

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, 15,
                60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("thread--xx--xx")
                        .setDaemon(true).build());
```

#### 5.2ListenableFuture

ListenableFuture顾名思义就是可以监听的Future，它是对java原生Future的扩展增强。我们知道Future表示一个异步计算任务，当任务完成时可以得到计算结果。如果我们希望一旦计算完成就拿到结果展示给用户或者做另外的计算，就必须使用另一个线程不断的查询计算状态。这样做，代码复杂，而且效率低下。使用ListenableFuture Guava帮我们检测Future是否完成了，如果完成就自动调用回调函数，这样可以减少并发程序的复杂度。

对应 JDK 中的 ExecutorService.submit(Callable) 提交多线程异步运算的方式，Guava 提供了 ListeningExecutorService 接口, 该接口返回 ListenableFuture 而相应的 ExecutorService 返回普通的 Future。将 ExecutorService 转为 ListeningExecutorService，可以使用MoreExecutors.listeningDecorator(ExecutorService)进行装饰。

```java
class MyTask implements Callable<Integer> {
        String str;

        public MyTask(String str) {
            this.str = str;
        }

        @Override
        public Integer call() throws Exception {
            System.out.println("call excute...." + str);
            return 8;
        }
    }

    @Test
    public void testListenableFeature() throws ExecutionException, InterruptedException {
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                MoreExecutors.getExitingExecutorService(new ThreadPoolExecutor(10, 100,
                        3000, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>())));
        ListenableFuture<Integer> future = executorService.submit(new MyTask("test"));
        System.out.println("future:" + future.get());

        Futures.addCallback(future, new FutureCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                System.out.println("result" + result);
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("error");
            }
        }, executorService);

    }
```

#### 5.3SettableFuture

settableFuture我们可以认为是一种异步转同步的工具。

我们先使用create()创建一个SettableFuture的实例,然后等线程获取结果后手动将返回值放入到settableFuture中,类似于一个ThreadLocal,一切看起来都很自然。作用就是get()方法,我们可以设置超时获取的时间,如果在指定时间内获取不到,则抛出异常。

```java
SettableFuture<Object> sf = SettableFuture.create();

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                MoreExecutors.getExitingExecutorService(new ThreadPoolExecutor(10, 100,
                        3000, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>())));
        ListenableFuture<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                TimeUnit.SECONDS.sleep(2);
                //TimeUnit.SECONDS.sleep(5);
                String ret = "hello";
                sf.set(ret);
                return ret;
            }
        });

        System.out.println(sf.get(5,TimeUnit.SECONDS));
```

我们在做网络通信时,假设一般请求-响应都是在3秒以内拿到结果.但有的时候遇到网络动荡、硬件性能问题时会导致超时,5秒,10秒甚至更多。但是我们希望超过5秒的请求我们就认为失败,这个时候SettableFuture就可以出场了,代码优雅,又能解决实际问题。

#### 5.4AsyncFunction

使用`ListenableFuture` 最重要的理由是它可以进行一系列的复杂链式的异步操作。

AsyncFunction接口常被用于当我们想要异步的执行转换而不造成线程阻塞时，尽管Future.get()方法会在任务没有完成时造成阻塞。

## 6. 字符串处理[Strings]

非常有用的字符串工具，包括分割、连接、填充等操作

#### 6.1 字符串连接

Joiner类用于字符串连接，可对数组、List、Map等连接为字符串，常用方法有：

| 方法                    | 使用                      |
| ----------------------- | ------------------------- |
| on()                    | 指定连接符                |
| skipNulls()             | 忽略null                  |
| useForNull()            | 对null使用替代符          |
| withKeyValueSeparator() | 键值对中（map中）的分隔符 |
| join()                  | 需连接的对象(list、map等  |

示例如下：

```java
//字符串连接，skipNulls去掉null
//one,tow
String s1 = Joiner.on(",").skipNulls().join("one", null, "tow");
//字符串连接，useForNull对null使用默认值代替
//one,default,tow
String s2 = Joiner.on(",").useForNull("default").join("one", null, "tow");
//map转为字符串连接，on为entry间分隔符，withKeyValueSeparator为kev和value间分隔符
//one=1&two=2
String s3 = Joiner.on("&").withKeyValueSeparator("=").join(ImmutableMap.of("one", "1", "two", "2"));
```

#### 6.2 字符串切分

Splitter类可对字符串切分为List、Map。常用方法有：

| 方法                    | 使用                                     |
| ----------------------- | ---------------------------------------- |
| on()                    | 指定分隔符                               |
| onPattern()             | 使用正则作为分隔符                       |
| fixedLength()           | 固定长度切分                             |
| withKeyValueSeparator() | 指定key/value的分隔符，用于切分为Map类型 |
| trimResults()           | 去掉首尾空白符                           |
| omitEmptyStrings()      | 忽略空串                                 |
| limit()                 | 限制切分的个数                           |
| splitToList()           | 切分为List                               |
| split()                 | 切分                                     |

```java
String rawStr="好好学习  study ,hard 123 2323 ，<b> </b>!  sf!!！桂\t林S\nSDF\rSSD  、DF   alter(test)<sef8989。，奔啊 lalsdl";
//单个分隔符（可单字或多字）分隔
List<String> list1 = Splitter.on("。").trimResults().omitEmptyStrings().splitToList(rawStr);
//正则分隔
List<String> list2 = Splitter.onPattern("\\s").trimResults().omitEmptyStrings().splitToList(rawStr);
//多个分隔符分隔
List<String> list3 = Splitter.on(CharMatcher.anyOf(",，")).trimResults().omitEmptyStrings().splitToList(rawStr);
//fixedLength固定长度切分，limit限制最大分隔量
List<String> list4 = Splitter.fixedLength(8).trimResults().omitEmptyStrings().limit(6).splitToList(rawStr);
//字符串转为map,on切分字符串，withKeyValueSeparator分隔key和value
Map<String, String> map1 = Splitter.on(",").withKeyValueSeparator("=").split("one=1,two=2,three=3,four=4");
```

#### 6.3 字符串匹配处理

CharMatcher类可进行字符串匹配抽取，流程是

`匹配目标串 --> 对匹配串操作`

匹配目标串常用方法有：

| 方法             | 使用             |
| ---------------- | ---------------- |
| inRange()        | 指定匹配字符区间 |
| whitespace（）   | 匹配空白符       |
| ascii()          | 匹配ascci码      |
| javaIsoControl() | 匹配ios控制符    |
| is()             | 匹配指定字符     |
| isNot()          | 不匹配指定字符   |
| anyOf()          | 匹配任意字符     |
| noneOf（）       | 不匹配任意字符   |

还可使用or(或)/negate(取反)/and(并)进行逻辑组合。
对匹配串操作的常用方法有：

| 方法           | 使用                             |
| -------------- | -------------------------------- |
| retainFrom()   | 保留匹配串                       |
| removeFrom()   | 删除匹配串                       |
| replaceFrom()  | 替换匹配串                       |
| collapseFrom() | 去掉连接匹配串，并用指定字符代替 |
| trimFrom()     | 删除首尾匹配串                   |
| countIn()      | 统计匹配串                       |
| matchesAnyOf() | 判断是否匹配任一字符             |

```java
String rawStr="好好学习  study hard 123 2323 <b> </b>!  sf!!！桂\t林S\nSDF\rSSD  DF   alter(test)<sef8989。，奔啊 lalsdl";
//在原始串中保留指定区间内的字符
result = CharMatcher.inRange('0', '9').retainFrom(rawStr);
//去掉空白或指定字符
result = CharMatcher.whitespace().or(CharMatcher.anyOf("<>!/，。")).removeFrom(rawStr);
//替换非\t\n\r的字符
result = CharMatcher.javaIsoControl().negate().replaceFrom(rawStr, "default");
//去掉首尾ascii码字符
result = CharMatcher.ascii().trimFrom(rawStr);
//统计ascii码字符
int cnt = CharMatcher.ascii().countIn(rawStr);
//判断ascii码字符
boolean flag = CharMatcher.ascii().matchesAnyOf(rawStr);
//去掉连续指定字符并用指定字符替换
result = CharMatcher.whitespace().trimAndCollapseFrom(rawStr, ' ');
```



#### 6.4 字符串编码

Charsets类提供了常用编码工具类，示例如：

```java
byte[] b1 = "study".getBytes(Charsets.UTF_8);
```

#### 6.5 属性名转换

CaseFormat提供了属性名转换的方法，常用属性格式有：

| 属性名格式                  | 解析                           |
| --------------------------- | ------------------------------ |
| CaseFormat.LOWER_UNDERSCOR  | 小写下划线，如lower_underscore |
| CaseFormat.LOWER_CAMEL      | 小写驼峰,如lowerCamel          |
| CaseFormat.LOWER_HYPHEN     | 小写中划线,如lower_hyphen      |
| CaseFormat.UPPER_CAMEL      | 大写驼峰，如UpperCamel         |
| CaseFormat.UPPER_UNDERSCORE | 大写下划线，如UPPER_UNDERSCORE |

示例如下：

```java
//小下划线转为小写驼峰，返回值为stuName
result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "stu_name");
//小写驼峰转为下划线，返回值为stu_name
result = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "stuName");
//小驼峰转为小写中划线，返回值为stu-name
result = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, "stuName");
//小写驼峰转为大写驼峰，返回值为StuName
result = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, "stuName");
//小写驼峰转为大写下划线，返回值为STU_NAME
result = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, "stuName");
```

#### 6.6 字符串处理

Strings类提供了字符串的处理，包括相同前后缀提取、字符填充、格式化等。

```java
//获取相同前缀
result = Strings.commonPrefix("aacdd22","aacex22");
//获取相同后缀
result = Strings.commonSuffix("aacdd22","aacex22");
//开头字符填充
result = Strings.padStart("aa",8,'x');
//结尾字符填充
result = Strings.padEnd("aa",8,'x');
//结尾字符填充
result = Strings.repeat("ax",3);
//格式化字符串
result = Strings.lenientFormat("%s+%s=%s", 1,2,3);
```

## 7. 原生类型

扩展 JDK 未提供的原生类型（如int、char）操作， 包括某些类型的无符号形式

Java的原生类型就是指基本类型：byte、short、int、long、float、double、char和boolean。

*在从Guava查找原生类型方法之前，可以先查查[Arrays](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Arrays.html)*类，或者对应的基础类型包装类，如[Integer](http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Integer.html)。

原生类型不能当作对象或泛型的类型参数使用，这意味着许多通用方法都不能应用于它们。Guava提供了若干通用工具，包括原生类型数组与集合API的交互，原生类型和字节数组的相互转换，以及对某些原生类型的无符号形式的支持。

| **原生类型** | **Guava**工具类（都在com.google.common.primitives包）        |
| ------------ | ------------------------------------------------------------ |
| byte         | [`Bytes`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/Bytes.html), [`SignedBytes`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/SignedBytes.html), [`UnsignedBytes`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/UnsignedBytes.html) |
| short        | [`Shorts`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/Shorts.html) |
| int          | [`Ints`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/Ints.html), [`UnsignedInteger`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/UnsignedInteger.html), [`UnsignedInts`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/UnsignedInts.html) |
| long         | [`Longs`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/Longs.html), [`UnsignedLong`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/UnsignedLong.html), [`UnsignedLongs`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/UnsignedLongs.html) |
| float        | [`Floats`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/Floats.html) |
| double       | [`Doubles`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/Doubles.html) |
| char         | [`Chars`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/Chars.html) |
| boolean      | [`Booleans`](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/primitives/Booleans.html) |

上面这些工具类(Bytes、Ints等等)里面的工具方法都大体相同，很多连名字都是一样的，所以这里我们就值只列出Ints里面的一些常用方法。如下：

| Ints static方法                                              | 解释                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| int checkedCast(long value)                                  | long转换为int,给定的值超过了int最大值，最小值则IllegalArgumentException |
| int saturatedCast(long value)                                | long转换为int。超过最大值为Integer.MAX_VALUE，小于最小值Integer.MIN_VALUE |
| int compare(int a, int b)                                    | 比较                                                         |
| boolean contains(int[] array, int target)                    | 是否包含                                                     |
| int indexOf(int[] array, int target)                         | 数组下标                                                     |
| int indexOf(int[] array, int[] target)                       | 数组下标                                                     |
| int lastIndexOf(int[] array, int target)                     | 最后一次出现的下标                                           |
| int min(int… array)                                          | 最小值                                                       |
| int max(int… array)                                          | 最大值                                                       |
| int constrainToRange(int value, int min, int max)            | [min,max]区间里面最接近value的值                             |
| int[] concat(int[]… arrays)                                  | 多个数组组合成一个数组                                       |
| byte[] toByteArray(int value)                                | int转byte数组                                                |
| int fromByteArray(byte[] bytes)                              | byte数组转int                                                |
| int fromBytes(byte b1, byte b2, byte b3, byte b4)            | byte转int                                                    |
| Converter<String, Integer> stringConverter()                 | String转int                                                  |
| int[] ensureCapacity(int[] array, int minLength, int padding) | 返回一个包含与array相同值的数组，如果原数组长度小于minLength则进行拷贝操作并且保证新数组长度为minLength + padding，否则直接返回原数组 |
| String join(String separator, int… array)                    | 多个int拼接成一个字符串                                      |
| Comparator<int[]> lexicographicalComparator()                | 返回一个比较int数组的比较器                                  |
| void sortDescending(int[] array)                             | 降序排序                                                     |
| void sortDescending(int[] array, int fromIndex, int toIndex) | 降序排序                                                     |
| void reverse(int[] array)                                    | 反转                                                         |
| void reverse(int[] array, int fromIndex, int toIndex)        | 反转                                                         |
| int[] toArray(Collection<? extends Number> collection)       | 转换成数组                                                   |
| List asList(int… backingArray)                               | 转换成list                                                   |
| Integer tryParse(String string)                              | string转int                                                  |
| Integer tryParse(String string, int radix)                   | string转int                                                  |

## 8. 区间[Ranges]

可比较类型的区间API，包括连续和离散类型

#### 8.1简介

区间，有时也称为范围，是特定域中的凸性（非正式说法为连续的或不中断的）部分。在形式上，凸性表示对a<=b<=c, range.contains(a)且range.contains(c)意味着range.contains(b)。

区间可以延伸至无限——例如，范围”x>3″包括任意大于3的值——也可以被限制为有限，如” 2<=x<5″。Guava用更紧凑的方法表示范围，有数学背景的程序员对此是耳熟能详的：

- (a..b) = {x | a < x < b}
- [a..b] = {x | a <= x <= b}
- [a..b) = {x | a <= x < b}
- (a..b] = {x | a < x <= b}
- (a..+∞) = {x | x > a}
- [a..+∞) = {x | x >= a}
- (-∞..b) = {x | x < b}
- (-∞..b] = {x | x <= b}
- (-∞..+∞) = 所有值

上面的a、b称为端点 。为了提高一致性，Guava中的Range要求上端点不能小于下端点。上下端点有可能是相等的，但要求区间是闭区间或半开半闭区间（至少有一个端点是包含在区间中的）：

- [a..a]：单元素区间
- [a..a); (a..a]：空区间，但它们是有效的
- (a..a)：无效区间

Guava用类型Range表示区间。所有区间实现都是不可变类型。

#### 8.2构建区间

区间实例可以由Range类的静态方法获取：

| (a..b)   | open(C, C)       |
| -------- | ---------------- |
| [a..b]   | closed(C, C)     |
| [a..b)   | closedOpen(C, C) |
| (a..b]   | openClosed(C, C) |
| (a..+∞)  | greaterThan(C)   |
| [a..+∞)  | atLeast(C)       |
| (-∞..b)  | lessThan(C)      |
| (-∞..b]  | atMost(C)        |
| (-∞..+∞) | all()            |

```java
Range.closed("left", "right"); //字典序在"left"和"right"之间的字符串，闭区间
Range.lessThan(4.0); //严格小于4.0的double值
```

此外，也可以明确地指定边界类型来构造区间：

| 有界区间                        | range(C, BoundType, C,  BoundType) |
| ------------------------------- | ---------------------------------- |
| 无上界区间：((a..+∞) 或[a..+∞)) | downTo(C, BoundType)               |
| 无下界区间：((-∞..b) 或(-∞..b]) | upTo(C, BoundType)                 |

这里的BoundType是一个枚举类型，包含CLOSED和OPEN两个值。

```java
Range.closed(1, 3).contains(2);//return true
Range.closed(1, 3).contains(4);//return false
Range.lessThan(5).contains(5); //return false
Range.closed(1, 4).containsAll(Ints.asList(1, 2, 3)); //return true
```

#### 8.3区间运算

Range的基本运算是它的contains(C) 方法，和你期望的一样，它用来区间判断是否包含某个值。此外，Range实例也可以当作Predicate，并且在函数式编程中使用（译者注：见第4章）。任何Range实例也都支持containsAll(Iterable<? extends C>)方法：

```java
Range.closed(1, 3).contains(2);//return true
Range.closed(1, 3).contains(4);//return false
Range.lessThan(5).contains(5); //return false
Range.closed(1, 4).containsAll(Ints.asList(1, 2, 3)); //return true
```

##### 8.3.1查询运算

Range类提供了以下方法来 查看区间的端点：

- hasLowerBound()和hasUpperBound()：判断区间是否有特定边界，或是无限的；
- lowerBoundType()和upperBoundType()：返回区间边界类型，CLOSED或OPEN；如果区间没有对应的边界，抛出IllegalStateException；
- lowerEndpoint()和upperEndpoint()：返回区间的端点值；如果区间没有对应的边界，抛出IllegalStateException；
- isEmpty()：判断是否为空区间。

```java
Range.closedOpen(4, 4).isEmpty(); // returns true
Range.openClosed(4, 4).isEmpty(); // returns true
Range.closed(4, 4).isEmpty(); // returns false
Range.open(4, 4).isEmpty(); // Range.open throws IllegalArgumentException
Range.closed(3, 10).lowerEndpoint(); // returns 3
Range.open(3, 10).lowerEndpoint(); // returns 3
Range.closed(3, 10).lowerBoundType(); // returns CLOSED
Range.open(3, 10).upperBoundType(); // returns OPEN
```

##### 8.3.2关系运算

> **包含[enclose]**

区间之间的最基本关系就是包含[`encloses(Range)`]：如果内区间的边界没有超出外区间的边界，则外区间包含内区间。包含判断的结果完全取决于区间端点的比较！

- [3..6] 包含[4..5] ；
- (3..6) 包含(3..6) ；
- [3..6] 包含[4..4)，虽然后者是空区间；
- (3..6]不 包含[3..6] ；
- [4..5]不 包含(3..6)，虽然前者包含了后者的所有值，离散域[discrete domains]可以解决这个问题（见8.5节）；
- [3..6]不 包含(1..1]，虽然前者包含了后者的所有值。

包含是一种偏序关系[partial ordering]。基于包含关系的概念，Range还提供了以下运算方法。

> **相连[isConnected]**

`Range.isConnected(Range)`判断区间是否是相连的。具体来说，isConnected测试是否有区间同时包含于这两个区间，这等同于数学上的定义”两个区间的并集是连续集合的形式”（空区间的特殊情况除外）。

相连是一种自反的[reflexive]、对称的[symmetric]关系。

```java
Range.closed(3, 5).isConnected(Range.open(5, 10)); // returns true
Range.closed(0, 9).isConnected(Range.closed(3, 4)); // returns true
Range.closed(0, 5).isConnected(Range.closed(3, 9)); // returns true
Range.open(3, 5).isConnected(Range.open(5, 10)); // returns false
Range.closed(1, 5).isConnected(Range.closed(6, 10)); // returns false
```

> **交集[intersection]**

`Range.intersection(Range)`返回两个区间的交集：既包含于第一个区间，又包含于另一个区间的最大区间。当且仅当两个区间是相连的，它们才有交集。如果两个区间没有交集，该方法将抛出IllegalArgumentException。

交集是可互换的 、关联的运算。

```java
Range.closed(3, 5).intersection(Range.open(5, 10)); // returns (5, 5]
Range.closed(0, 9).intersection(Range.closed(3, 4)); // returns [3, 4]
Range.closed(0, 5).intersection(Range.closed(3, 9)); // returns [3, 5]
Range.open(3, 5).intersection(Range.open(5, 10)); // throws IAE
Range.closed(1, 5).intersection(Range.closed(6, 10)); // throws IAE
```

> **跨区间[span]**

`Range.span(Range)`返回”同时包括两个区间的最小区间”，如果两个区间相连，那就是它们的并集。

span是可互换的、关联的、闭合的运算。

```java
Range.closed(3, 5).span(Range.open(5, 10)); // returns [3, 10)
Range.closed(0, 9).span(Range.closed(3, 4)); // returns [0, 9]
Range.closed(0, 5).span(Range.closed(3, 9)); // returns [0, 9]
Range.open(3, 5).span(Range.open(5, 10)); // returns (3, 10)
Range.closed(1, 5).span(Range.closed(6, 10)); // returns [1, 10]
```

#### 8.4离散域

部分（但不是全部）可比较类型是离散的，即区间的上下边界都是可枚举的。

在Guava中，用DiscreteDomain实现类型C的离散形式操作。一个离散域总是代表某种类型值的全集；它不能代表类似”素数”、”长度为5的字符串”或”午夜的时间戳”这样的局部域。

DiscreteDomain提供的离散域实例包括：

| **类型** | **离散域** |
| -------- | ---------- |
| Integer  | integers() |
| Long     | longs()    |

一旦获取了DiscreteDomain实例，你就可以使用下面的Range运算方法：

- ContiguousSet.create(range, domain)：用ImmutableSortedSet<C>形式表示Range<C>中符合离散域定义的元素，并增加一些额外操作——*译者注：实际返回**ImmutableSortedSet**的子类**ContiguousSet*。（对无限区间不起作用，除非类型C本身是有限的，比如int就是可枚举的）
- canonical(domain)：把离散域转为区间的”规范形式”。如果ContiguousSet.create(a, domain).equals(ContiguousSet.create(b, domain))并且!a.isEmpty()，则有a.canonical(domain).equals(b.canonical(domain))。（这并不意味着a.equals(b)）

```java
ImmutableSortedSet set = ContigousSet.create(Range.open(1, 5), iscreteDomain.integers());
//set包含[2, 3, 4]
ContiguousSet.create(Range.greaterThan(0), DiscreteDomain.integers());
//set包含[1, 2, ..., Integer.MAX_VALUE]
```

## 9. I/O

简化I/O尤其是I/O流和文件的操作，针对Java5和6版本

#### 9.1字节流和字符流

Guava使用术语”流” 来表示可关闭的，并且在底层资源中有位置状态的I/O数据流。术语”字节流”指的是InputStream或OutputStream，”字符流”指的是Reader 或Writer（虽然他们的接口Readable 和Appendable被更多地用于方法参数）。相应的工具方法分别在`ByteStreams`和`CharStreams`中。

大多数Guava流工具一次处理一个完整的流，并且/或者为了效率自己处理缓冲。还要注意到，接受流为参数的Guava方法不会关闭这个流：关闭流的职责通常属于打开流的代码块。

其中的一些工具方法列举如下：

| **ByteStreams**                        | **CharStreams**                   |
| -------------------------------------- | --------------------------------- |
| `byte[] toByteArray(InputStream)`      | `String toString(Readable)`       |
| N/A                                    | `List readLines(Readable)`        |
| `long copy(InputStream, OutputStream)` | `long copy(Readable, Appendable)` |
| `void readFully(InputStream, byte[])`  | N/A                               |
| `void skipFully(InputStream, long)`    | `void skipFully(Reader, long)`    |
| `OutputStream nullOutputStream()`      | `Writer nullWriter()`             |

#### 9.2源与汇

通常我们都会创建I/O工具方法，这样可以避免在做基础运算时总是直接和流打交道。例如，Guava有Files.toByteArray(File) 和Files.write(File, byte[])。然而，流工具方法的创建经常最终导致散落各处的相似方法，每个方法读取不同类型的源

或写入不同类型的汇[sink]。例如，Guava中的Resources.toByteArray(URL)和Files.toByteArray(File)做了同样的事情，只不过数据源一个是URL，一个是文件。

为了解决这个问题，Guava有一系列关于源与汇的抽象。源或汇指某个你知道如何从中打开流的资源，比如File或URL。源是可读的，汇是可写的。此外，源与汇按照字节和字符划分类型。

|        | **字节**     | **字符**     |
| ------ | ------------ | ------------ |
| **读** | `ByteSource` | `CharSource` |
| **写** | `ByteSink`   | `CharSink`   |

源与汇API的好处是它们提供了通用的一组操作。比如，一旦你把数据源包装成了ByteSource，无论它原先的类型是什么，你都得到了一组按字节操作的方法。

> ##### 创建源与汇

Guava提供了若干源与汇的实现：

| **字节**                                   | **字符**                                            |
| ------------------------------------------ | --------------------------------------------------- |
| `Files.asByteSource(File)`                 | `Files.asCharSource(File, Charset)`                 |
| `Files.asByteSink(File, FileWriteMode...)` | `Files.asCharSink(File, Charset, FileWriteMode...)` |
| `Resources.asByteSource(URL)`              | `Resources.asCharSource(URL, Charset)`              |
| `ByteSource.wrap(byte[\])`                 | `CharSource.wrap(CharSequence)`                     |
| `ByteSource.concat(ByteSource...)`         | `CharSource.concat(CharSource...)`                  |
| `ByteSource.slice(long, long)`             | N/A                                                 |
| N/A                                        | `ByteSource.asCharSource(Charset)`                  |
| N/A                                        | `ByteSink.asCharSink(Charset)`                      |

此外，你也可以继承这些类，以创建新的实现。

注：把已经打开的流（比如InputStream）包装为源或汇听起来是很有诱惑力的，但是应该避免这样做。源与汇的实现应该在每次openStream()方法被调用时都创建一个新的流。始终创建新的流可以让源或汇管理流的整个生命周期，并且让多次调用openStream()返回的流都是可用的。此外，如果你在创建源或汇之前创建了流，你不得不在异常的时候自己保证关闭流，这压根就违背了发挥源与汇API优点的初衷。

> ##### 使用源与汇

一旦有了源与汇的实例，就可以进行若干读写操作。

```java
//Read the lines of a UTF-8 text file
ImmutableList<String> lines = Files.asCharSource(file, Charsets.UTF_8).readLines();
//Count distinct word occurrences in a file
Multiset<String> wordOccurrences = HashMultiset.create(
        Splitter.on(CharMatcher.WHITESPACE)
            .trimResults()
            .omitEmptyStrings()
            .split(Files.asCharSource(file, Charsets.UTF_8).read()));

//SHA-1 a file
HashCode hash = Files.asByteSource(file).hash(Hashing.sha1());

//Copy the data from a URL to a file
Resources.asByteSource(url).copyTo(Files.asByteSink(file));
```

## 10. 散列[Hash]

提供比`Object.hashCode()`更复杂的散列实现，并提供布鲁姆过滤器的实现

> #### 概述

Java内建的散列码[hash code]概念被限制为32位，并且没有分离散列算法和它们所作用的数据，因此很难用备选算法进行替换。此外，使用Java内建方法实现的散列码通常是劣质的，部分是因为它们最终都依赖于JDK类中已有的劣质散列码。

Object.hashCode往往很快，但是在预防碰撞上却很弱，也没有对分散性的预期。这使得它们很适合在散列表中运用，因为额外碰撞只会带来轻微的性能损失，同时差劲的分散性也可以容易地通过再散列来纠正（Java中所有合理的散列表都用了再散列方法）。然而，在简单散列表以外的散列运用中，Object.hashCode几乎总是达不到要求——因此，有了com.google.common.hash包。

我们先来看下面这段代码范例： 

```java
HashFunction hf = Hashing.md5();
HashCode hc = hf.newHasher()
        .putLong(id)
        .putString(name, Charsets.UTF_8)
        .putObject(person, personFunnel)
        .hash();
```

> ##### HashFunction

HashFunction是一个单纯的（引用透明的）、无状态的方法，它把任意的数据块映射到固定数目的位指，并且保证相同的输入一定产生相同的输出，不同的输入尽可能产生不同的输出。

> ##### Hasher

HashFunction的实例可以提供有状态的Hasher，`Hasher提供了流畅的语法把数据添加到散列运算`，然后获取散列值。Hasher可以接受所有原生类型、字节数组、字节数组的片段、字符序列、特定字符集的字符序列等等，或者任何给定了Funnel实现的对象。

Hasher实现了PrimitiveSink接口，这个接口为接受原生类型流的对象定义了fluent风格的API

> ##### Funnel

Funnel描述了如何把一个具体的对象类型分解为原生字段值，从而写入PrimitiveSink。比如，如果我们有这样一个类：

```java
class Person {
    final int id;
    final String firstName;
    final String lastName;
    final int birthYear;
}
```

它对应的Funnel实现可能是：

```java
Funnel<Person> personFunnel = new Funnel<Person>() {
    @Override
    public void funnel(Person person, PrimitiveSink into) {
        into
            .putInt(person.id)
            .putString(person.firstName, Charsets.UTF_8)
            .putString(person.lastName, Charsets.UTF_8)
            .putInt(birthYear);
    }
}
```

> ##### HashCode

一旦Hasher被赋予了所有输入，就可以通过hash()方法获取HashCode实例（多次调用hash()方法的结果是不确定的）。HashCode可以通过asInt()、asLong()、asBytes()方法来做相等性检测，此外，writeBytesTo(array, offset, maxLength)把散列值的前maxLength字节写入字节数组。

> 布鲁姆过滤器[BloomFilter]

布鲁姆过滤器是哈希运算的一项优雅运用，它可以简单地基于Object.hashCode()实现。简而言之，布鲁姆过滤器是一种概率数据结构，它允许你检测某个对象是一定不在过滤器中，还是可能已经添加到过滤器了。

Guava散列包有一个内建的布鲁姆过滤器实现，你只要提供Funnel就可以使用它。你可以使用create(Funnel funnel, int expectedInsertions, double falsePositiveProbability)方法获取BloomFilter<T>，缺省误检率[falsePositiveProbability]为3%。BloomFilter<T>提供了boolean mightContain(T) 和void put(T)，它们的含义都不言自明了。

```java
BloomFilter<Person> friends = BloomFilter.create(personFunnel, 500, 0.01);
for(Person friend : friendsList) {
    friends.put(friend);
}

// 很久以后
if (friends.mightContain(dude)) {
    //dude不是朋友还运行到这里的概率为1%
    //在这儿，我们可以在做进一步精确检查的同时触发一些异步加载
}
```

## 11.事件总线[EventBus]

发布-订阅模式的组件通信，但组件不需要显式地注册到其他组件中

传统上，Java的进程内事件分发都是通过发布者和订阅者之间的显式注册实现的。设计EventBus就是为了取代这种显示注册方式，使组件间有了更好的解耦。EventBus不是通用型的发布-订阅实现，不适用于进程间通信。

范例:

```java
// Class is typically registered by the container.
class EventBusChangeRecorder {
    @Subscribe public void recordCustomerChange(ChangeEvent e) {
        recordChange(e.getChange());
    }
}
// somewhere during initialization
eventBus.register(new EventBusChangeRecorder());
// much later
public void changeCustomer() {
    ChangeEvent event = getChangeEvent();
    eventBus.post(event);
}
```

> ## 一分钟指南

把已有的进程内事件分发系统迁移到EventBus非常简单。

> #### 事件监听者[Listeners]

监听特定事件（如，CustomerChangeEvent）：

- 传统实现：定义相应的事件监听者类，如CustomerChangeEventListener；
- EventBus实现：以CustomerChangeEvent为唯一参数创建方法，并用`Subscribe`注解标记。

`Guava默认订阅者的消费方法不具有并发能力，如果你的消费方法具有并发能力，可以使用@AllowConcurrentEvents注解，用来标记当前订阅者是线程安全的，支持并发接收事件消息。`

把事件监听者注册到事件生产者：

- 传统实现：调用事件生产者的registerCustomerChangeEventListener方法；这些方法很少定义在公共接口中，因此开发者必须知道所有事件生产者的类型，才能正确地注册监听者；
- EventBus实现：在EventBus**实例**上调用EventBus.register(Object)方法；请保证事件生产者和监听者共享相同的EventBus**实例**。

按事件超类监听（如，EventObject甚至Object）：

- 传统实现：很困难，需要开发者自己去实现匹配逻辑；
- EventBus实现：EventBus自动把事件分发给事件超类的监听者，并且允许监听者声明监听接口类型和泛型的通配符类型（wildcard，如 ? super XXX）。

检测没有监听者的事件：

- 传统实现：在每个事件分发方法中添加逻辑代码（也可能适用AOP）；
- EventBus实现：监听DeadEvent；EventBus会把所有发布后没有监听者处理的事件包装为DeadEvent（对调试很便利）。

> #### 事件生产者[Producers]

管理和追踪监听者：

- 传统实现：用列表管理监听者，还要考虑线程同步；或者使用工具类，如EventListenerList；
- EventBus实现：EventBus内部已经实现了监听者管理。

向监听者分发事件：

- 传统实现：开发者自己写代码，包括事件类型匹配、异常处理、异步分发；
- EventBus实现：把事件传递给 EventBus.post(Object)方法。异步分发可以直接用EventBus的子类AsyncEventBus。

> #### 术语表

事件总线系统使用以下术语描述事件分发：

| 事件     | 可以向事件总线发布的对象                                     |
| -------- | ------------------------------------------------------------ |
| 订阅     | 向事件总线注册*监听者*以接受事件的行为                       |
| 监听者   | 提供一个*处理方法*，希望接受和处理事件的对象                 |
| 处理方法 | 监听者提供的公共方法，事件总线使用该方法向监听者发送事件；该方法应该用Subscribe注解 |
| 发布消息 | 通过事件总线向所有匹配的监听者提供事件                       |

## 12.数学运算[Math]

优化的、充分测试的数学工具类

> 为什么使用Guava Math

- Guava Math针对各种不常见的溢出情况都有充分的测试；对溢出语义，Guava文档也有相应的说明；如果运算的溢出检查不能通过，将导致快速失败；

- Guava Math的性能经过了精心的设计和调优；虽然性能不可避免地依据具体硬件细节而有所差异，但Guava Math的速度通常可以与Apache Commons的MathUtils相比，在某些场景下甚至还有显著提升；

- Guava Math在设计上考虑了可读性和正确的编程习惯；IntMath.log2(x, CEILING) 所表达的含义，即使在快速阅读时也是清晰明确的。而32-Integer.numberOfLeadingZeros(x – 1)对于阅读者来说则不够清晰。

注意：Guava Math和GWT格外不兼容，这是因为Java和Java Script语言的运算溢出逻辑不一样。

#### 12.1整数运算

Guava Math主要处理三种整数类型：int、long和BigInteger。这三种类型的运算工具类分别叫做IntMath、LongMath和BigIntegerMath。

> 有溢出检查的运算

如果计算结果有溢出的情况下(上溢，下溢)，就会抛出ArithmeticException异常。

| 运算(有溢出检查) | IntMath里面方法               | LongMath里面方法              |
| ---------------- | ----------------------------- | ----------------------------- |
| 加法             | checkedAdd(int a, int b)      | checkedAdd(int a, int b)      |
| 减法             | checkedSubtract(int a, int b) | checkedSubtract(int a, int b) |
| 乘法             | checkedMultiply(int a, int b) | checkedMultiply(int a, int b) |
| 幂               | checkedPow(int b, int k)      | checkedPow(int b, int k)      |

> 上溢，下溢返回最大值最小值

如果对应的运算如果发生溢出，上溢则返回对应类型的最大值(Integer.MAX_VALUE、Long.MAX_VALUE )、下溢则返回对应类型的最小值(Integer.MIN_VALUE、Long.MIN_VALUE)。

| 运算 | IntMath里面方法                 | LongMath里面方法                |
| ---- | ------------------------------- | ------------------------------- |
| 加法 | saturatedAdd(int a, int b)      | saturatedAdd(int a, int b)      |
| 减法 | saturatedSubtract(int a, int b) | saturatedSubtract(int a, int b) |
| 乘法 | saturatedMultiply(int a, int b) | saturatedMultiply(int a, int b) |
| 幂   | saturatedPow(int b, int k)      | saturatedPow(int b, int k)      |

#### 12.2实数运算

IntMath、LongMath和BigIntegerMath提供了很多实数运算的方法，并把最终运算结果舍入成整数。这些方法需要指定一个java.math.RoundingMode枚举值来作为舍入的模式。RoundingMode的取值如下：

| RoundingMode枚举值       | 解释                                                         |
| ------------------------ | ------------------------------------------------------------ |
| RoundingMode.DOW         | 向零方向舍入（去尾法）                                       |
| RoundingMode.UP          | 远离零方向舍入                                               |
| RoundingMode.FLOO        | 向负无限大方向舍入                                           |
| RoundingMode.CEILING     | 向正无限大方向舍入                                           |
| RoundingMode.UNNECESSARY | 不需要舍入，如果用此模式进行舍入，应直接抛出ArithmeticException |
| RoundingMode.HALF_UP     | 向最近的整数舍入，其中x.5远离零方向舍入                      |
| RoundingMode.HALF_DOWN   | 向最近的整数舍入，其中x.5向零方向舍入                        |
| RoundingMode.HALF_EVEN   | 向最近的整数舍入，其中x.5向相邻的偶数舍入                    |

> 实数运算方法

| 运算         | IntMath里面方法                | LongMath里面方法                 | BigIntegerMath里面方法                       |
| ------------ | ------------------------------ | -------------------------------- | -------------------------------------------- |
| 除法         | divide(int, int, RoundingMode) | divide(long, long, RoundingMode) | divide(BigInteger, BigInteger, RoundingMode) |
| 2为底的对数  | log2(int, RoundingMode)        | log2(long, RoundingMode)         | log2(BigInteger, RoundingMode)               |
| 10为底的对数 | log10(int, RoundingMode)       | log10(long, RoundingMode)        | log10(BigInteger, RoundingMode)              |
| 平方根       | sqrt(int, RoundingMode)        | sqrt(long, RoundingMode)         | sqrt(BigInteger, RoundingMode)               |

> 实数运算部分Guava还另外提供了一些有用的运算函数

| 运算        | IntMath里面方法    | LongMath里面方法   | BigIntegerMath里面方法   |
| ----------- | ------------------ | ------------------ | ------------------------ |
| 最大公约数  | gcd(int, int)      | gcd(long, long)    | gcd(BigInteger)          |
| 取模        | mod(int, int)      | mod(long, long)    | mod(BigInteger)          |
| 取幂        | pow(int, int)      | pow(long, int)     | pow(int)                 |
| 是否2的幂   | isPowerOfTwo(int)  | isPowerOfTwo(long) | isPowerOfTwo(BigInteger) |
| 阶乘*       | factorial(int)     | factorial(int)     | factorial(int)           |
| 二项式系数* | binomial(int, int) | binomial(int, int) | binomial(int, int)       |

*阶乘和二项式系数的运算结果如果溢出，则返回MAX_VALUE

#### 12.3浮点数运算

JDK已经比较彻底地涵盖了浮点数运算，但Guava在DoubleMath类中也提供了一些有用的方法。

| 运算                                                     | DoubleMath方法                          |
| -------------------------------------------------------- | --------------------------------------- |
| 判断该浮点数是不是一个整数                               | isMathematicalInteger(double)           |
| 舍入为int；对无限小数、溢出抛出异常                      | roundToInt(double, RoundingMode)        |
| 舍入为long；对无限小数、溢出抛出异常                     | roundToLong(double, RoundingMode)       |
| 舍入为BigInteger；对无限小数抛出异常                     | roundToBigInteger(double, RoundingMode) |
| 2的浮点对数，并且舍入为int，比JDK的Math.log(double) 更快 | log2(double, RoundingMode)              |

## 13.反射[Reflection]

Guava 的 Java 反射机制工具类

#### 13.1 TypeToken

Guava TypeToken类是用来帮我们解决java运行时泛型类型被擦除的问题的。

这里用一个具体的实例来解释下什么是类型檫除，特别是使用泛型的时候容易出现类型檫除。

```java
ArrayList<String> stringList = Lists.newArrayList();
ArrayList<Integer> intList = Lists.newArrayList();
System.out.println("intList type is " + intList.getClass());
System.out.println("stringList type is " + stringList.getClass());
```

上述代码虽然我们定义的是ArrayList、ArrayList类型的对象。但是两者的打印都是java.util.ArrayList。没办法去获取到String、Integer了。他们的类型被檫除了，在想通过类型来判断是哪个对象就做不到了。

鉴于类似这样的情况发生Guava给我们提供了TypeToken类。怎么用，

```java
// 认为stringList和intList的类型是一样的。这就是所谓的泛型类型擦除, 泛型String和Integer被檫除了。
System.out.println(stringList.getClass().isAssignableFrom(intList.getClass()));

// 定义了一个空的匿名类
TypeToken<ArrayList<String>> typeToken = new TypeToken<ArrayList<String>>() {
};
// TypeToken解析出了泛型参数的具体类型
TypeToken<?> genericTypeToken = typeToken.resolveType(ArrayList.class.getTypeParameters()[0]);
System.out.println(genericTypeToken.getType());
```

  TypeToken常用方法如下：

| 方法                   | 描述                                                         |
| ---------------------- | ------------------------------------------------------------ |
| getType()              | 获得包装的java.lang.reflect.Type.                            |
| getRawType()           | 返回大家熟知的运行时类                                       |
| getSubtype(Class<?>)   | 返回那些有特定原始类的子类型。举个例子，如果这有一个Iterable并且参数是List.class，那么返回将是List。 |
| getSupertype(Class<?>) | 产生这个类型的超类，这个超类是指定的原始类型。举个例子，如果这是一个Set并且参数是Iterable.class，结果将会是Iterable。 |
| isAssignableFrom(type) | 如果这个类型是 assignable from 指定的类型，并且考虑泛型参数，返回true。List<? extends Number>是assignable from List，但List没有. |
| getTypes()             | 返回一个Set，包含了这个所有接口，子类和类是这个类型的类。返回的Set同样提供了classes()和interfaces()方法允许你只浏览超类和接口类。 |
| isArray()              | 检查某个类型是不是数组，甚至是<? extends A[]>。              |
| getComponentType()     | 返回组件类型数组。                                           |

#### 13.2 Invokable

Guava的Invokable是对java.lang.reflect.Method和java.lang.reflect.Constructor的流式包装。它简化了常见的反射代码的使用。

#### 13.3动态代理(Dynamic Proxies)

​    Guava为了方便大家很好的处理动态代理，给大家做了两件事：简化生成代理对象的生成、提供了AbstractInvocationHandler了。

> 动态代理对象的生成

​    对于单一的接口类型需要被代理的时候。Guava简化了代理对象的生成。我们用一个具体的实例来说明

> 定义一个很简单的接口AddService

```java
public interface AddService {
    int add(int a, int b);
}
```

> 实现AddService

```java
public class AddServiceImpl implements AddService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
```

> 实现InvocationHandler接口(代理方法处理接口)。通过实现类里面的getProxy()
> 拿到代理对象。

原来JDK生成代理对象是通过Proxy.newProxyInstance()方法生成，这个方法需要三个参数：第一个参数指定产生代理对象的类加载器，需要将其指定为和目标对象同一个类加载器、第二个参数要实现和目标对象一样的接口，所以只需要拿到目标对象的实现接口、第三个参数表明这些被拦截的方法在被拦截时需要执行哪个InvocationHandler的invoke方法。

Guava生成代理对象。直接调用Reflection.newProxy()函数，这个函数也是需要两个参数：第一个参数接口，要实现和目标对象的某一样的接口(那个接口里面的方法需要代理)、第二个参数表明这些被拦截的方法在被拦截时需要执行哪个InvocationHandler的invoke方法。

