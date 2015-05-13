package net.prasenjit.poc.nashorn;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.gemfire.GemfireTransactionCommitException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheTransactionManager;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;

/**
 * Local transaction manager for GemFire Enterprise Fabric (GEF). Provides a
 * {@link PlatformTransactionManager} implementation for a single GemFire
 * {@link CacheTransactionManager}.
 * 
 * Binds one or multiple GemFire regions for the specified {@link Cache} to the
 * thread, potentially allowing for one region per cache model.
 * 
 * <p>
 * This local strategy is an alternative to executing cache operations within
 * JTA transactions. Its advantage is that is able to work in any environment,
 * for example a stand-alone application or a test suite. It is <i>not</i> able
 * to provide XA transactions, for example to share transactions with data
 * access.
 * 
 * <p>
 * To prevent dirty reads, by default, the cache is configured to return copies
 * rather then direct references for <code>get</code> operations. As a
 * workaround, one could use explicitly deep copy objects before making changes
 * to them to avoid unnecessary copying on every fetch.
 * 
 * @see CacheTransactionManager
 * @see Cache#setCopyOnRead(boolean)
 * @see Region#get(Object)
 * @see com.gemstone.gemfire.CopyHelper#copy(Object)
 * @see #setCopyOnRead(boolean)
 * @see AbstractPlatformTransactionManager
 * 
 * @author Costin Leau
 */
@SuppressWarnings("serial")
public class ClientCacheTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean,
		ResourceTransactionManager {

	private ClientCache cache;

	private boolean copyOnRead = true;

	/**
	 * Creates a new GemfireTransactionManager instance.
	 */
	public ClientCacheTransactionManager() {
	}

	/**
	 * Creates a new GemfireTransactionManager instance.
	 * 
	 * @param cache a reference to the GemFire Cache associated with Cache transactions.
	 */
	public ClientCacheTransactionManager(ClientCache cache) {
		this.cache = cache;
		afterPropertiesSet();
	}

	@Override
	public void afterPropertiesSet() {
		Assert.notNull(cache, "Cache property is required");
		cache.setCopyOnRead(copyOnRead);
	}

	@Override
	protected Object doGetTransaction() throws TransactionException {
		CacheTransactionObject txObject = new CacheTransactionObject();
		CacheHolder cacheHolder = (CacheHolder) TransactionSynchronizationManager.getResource(getCache());
		txObject.setHolder(cacheHolder);
		return txObject;
	}

	@Override
	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		CacheTransactionObject txObject = (CacheTransactionObject) transaction;
		// Consider a pre-bound cache as transaction.
		return (txObject.getHolder() != null);
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		CacheTransactionObject txObject = (CacheTransactionObject) transaction;

		ClientCache cache = null;

		try {
			cache = getCache();
			if (logger.isDebugEnabled()) {
				logger.debug("Acquired Cache [" + cache + "] for local Cache transaction");
			}

			txObject.setHolder(new CacheHolder());
			cache.getCacheTransactionManager().begin();
			TransactionSynchronizationManager.bindResource(cache, txObject.getHolder());
		}

		catch (IllegalStateException ex) {
			throw new CannotCreateTransactionException(
					"An ongoing transaction already is already associated with the current thread; are there multiple transaction managers ?",
					ex);
		}
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
		// CacheTransactionObject txObject = (CacheTransactionObject)
		// status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Committing Gemfire local transaction on Cache [" + cache + "]");
		}
		try {
			cache.getCacheTransactionManager().commit();
		}
		catch (IllegalStateException ex) {
			throw new NoTransactionException(
					"No transaction associated with the current thread; are there multiple transaction managers ?", ex);
		}
		
		catch (com.gemstone.gemfire.cache.TransactionException ex) {
			throw new GemfireTransactionCommitException("Unexpected failure on commit of Cache local transaction", ex);
		}
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
 		if (status.isDebug()) {
			logger.debug("Rolling back Cache local transaction for [" + cache + "]");
		}
		try {
			cache.getCacheTransactionManager().rollback();
		}
		catch (IllegalStateException ex) {
			throw new NoTransactionException(
					"No transaction associated with the current thread; are there multiple transaction managers ?", ex);
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		CacheTransactionObject txObject = (CacheTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting Gemfire local transaction [" + txObject.getHolder() + "] rollback-only");
		}
		txObject.getHolder().setRollbackOnly();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		// Remove the cache holder from the thread.
		TransactionSynchronizationManager.unbindResource(cache);
	}

	@Override
	protected final boolean useSavepointForNestedTransaction() {
		return false;
	}

	/**
	 * Returns the Cache that this instance manages local transactions for.
	 * 
	 * @return Gemfire cache
	 */
	public ClientCache getCache() {
		return cache;
	}

	/**
	 * Sets the Cache that this instance manages local transactions for.
	 * 
	 * @param cache Gemfire cache
	 */
	public void setCache(ClientCache cache) {
		this.cache = cache;
	}

	@Override
	public Object getResourceFactory() {
		return getCache();
	}

	/**
	 * Sets the GemFire Cache {@link Region} (as an alternative in setting in the Cache directly).
	 *
	 * @param <K> the Region key class type.
	 * @param <V> the Region value class type.
	 * @param region the Gemfire Cache Region directly involved in the Cache transaction.
	 */
	public <K, V> void setRegion(Region<K, V> region) {
		Assert.notNull(region, "non-null arguments are required");
		this.cache = (ClientCache)region.getRegionService();
	}

	/**
	 * Indicates whether the cache returns direct references or copies of the
	 * objects (default) it manages. While copies imply additional work for
	 * every fetch operation, direct references can cause dirty reads across
	 * concurrent threads in the same VM, whether or not transactions are used.
	 *
	 * One could explicitly deep copy objects before making changes (for example
	 * by using {@link com.gemstone.gemfire.CopyHelper#copy(Object)} in which
	 * case this setting can be set to <code>false</code>. However, unless there
	 * is a measurable performance penalty, the recommendation is to keep this
	 * setting to <code>true</code>
	 * 
	 * @param copyOnRead whether copies (default) rather then direct references
	 * will be returned on fetch operations
	 */
	public void setCopyOnRead(boolean copyOnRead) {
		this.copyOnRead = copyOnRead;
	}

	/**
	 * Indicates whether copy on read is set or not on the transaction manager.
	 * 
	 * @see #setCopyOnRead(boolean)
	 * @return the copyOnRead
	 */
	public boolean isCopyOnRead() {
		return copyOnRead;
	}

	/**
	 * GemfireTM local transaction object.
	 * 
	 * @author Costin Leau
	 */
	private static class CacheTransactionObject {
		private CacheHolder cacheHolder;

		public CacheHolder getHolder() {
			return cacheHolder;
		}

		public void setHolder(CacheHolder cacheHolder) {
			this.cacheHolder = cacheHolder;
		}
	}

	private static class CacheHolder {

		private boolean rollbackOnly = false;

		@SuppressWarnings("unused")
		public boolean isRollbackOnly() {
			return rollbackOnly;
		}

		public void setRollbackOnly() {
			rollbackOnly = true;
		}
	}
}

