(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('transaction-compte-pti', {
            parent: 'entity',
            url: '/transaction-compte-pti?page&sort&search',
            data: {
                authorities: ['ROLE_CONSULT_ALL_TRANSACTIONS'],
                pageTitle: 'ptiApp.transactionComptePti.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/components/tables/tables.html',
                    controller: 'TransactionComptePtiController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('transactionComptePti');
                    $translatePartialLoader.addPart('typeTransactionCompte');
                    $translatePartialLoader.addPart('etatValidite');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('transaction-compte-pti-detail', {
            parent: 'transaction-compte-pti',
            url: '/transaction-compte-pti/{id}',
            data: {
                authorities: ['ROLE_CONSULT_ALL_TRANSACTIONS'],
                pageTitle: 'ptiApp.transactionComptePti.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/transaction-compte-pti/transaction-compte-pti-detail.html',
                    controller: 'TransactionComptePtiDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('transactionComptePti');
                    $translatePartialLoader.addPart('typeTransactionCompte');
                    $translatePartialLoader.addPart('etatValidite');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'TransactionComptePti', function($stateParams, TransactionComptePti) {
                    return TransactionComptePti.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'transaction-compte-pti',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
            .state('transaction-pending', {
                parent: 'entity',
                url: '/transaction-pending',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/transaction-compte-pti/transaction-pending.html',
                        controller: 'TransactionPendingController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            })
            .state('journal-transaction', {
                parent: 'entity',
                url: '/journal-transaction',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/transaction-compte-pti/journal-transaction.html',
                        controller: 'JournalTransactionController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            });
    }

})();
