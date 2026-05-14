error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/CatalogAuctionAdapter.java
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/CatalogAuctionAdapter.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[12,38]

error in qdox parser
file content:
```java
offset: 474
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/CatalogAuctionAdapter.java
text:
```scala
package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionCatalogPort;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.MedicationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component // (o @Service)
public class CatalogAuctionAdapter { .@@.. }


import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogAuctionAdapter implements AuctionCatalogPort {

    private final MedicationRepositoryPort medicationRepository;

    @Override
    public Optional<MedicationInfo> getMedicationInfo(Long medicationId) {
        return medicationRepository.findById(medicationId)
                .map(m -> new MedicationInfo(m.getId(), m.getName(), m.getUnit()));
    }

    @Override
    public void reserveStock(Long branchId, Long medicationId) {
        medicationRepository.findStockByBranch(branchId).stream()
                .filter(s -> medicationId.equals(s.getMedicationId()))
                .findFirst()
                .ifPresentOrElse(
                    s -> {
                        int newQty = Math.max(0, s.getQuantity() - 1);
                        medicationRepository.updateStock(branchId, medicationId, newQty);
                        log.info("Stock reservado - sucursal={}, medicamento={}, nuevo stock={}",
                                branchId, medicationId, newQty);
                    },
                    () -> log.warn("No se encontró stock para reservar - sucursal={}, medicamento={}",
                            branchId, medicationId)
                );
    }

    @Override
    public void releaseStock(Long branchId, Long medicationId) {
        medicationRepository.findStockByBranch(branchId).stream()
                .filter(s -> medicationId.equals(s.getMedicationId()))
                .findFirst()
                .ifPresentOrElse(
                    s -> {
                        medicationRepository.updateStock(branchId, medicationId, s.getQuantity() + 1);
                        log.info("Stock liberado - sucursal={}, medicamento={}", branchId, medicationId);
                    },
                    () -> log.warn("No se encontró stock para liberar - sucursal={}, medicamento={}",
                            branchId, medicationId)
                );
    }
}

```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

QDox parse error in file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/CatalogAuctionAdapter.java