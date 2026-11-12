(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('bw-customer', {
            parent: 'entity',
            url: '/bw-customer',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'partnerApp.bwCustomer.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bw-customer/bw-customers.html',
                    controller: 'BwCustomerController',
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
                    $translatePartialLoader.addPart('bwCustomer');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
           
        })
        .state('bw-customer-detail', {
            parent: 'bw-customer',
            url: '/bw-customer/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.bwCustomer.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bw-customer/bw-customer-detail.html',
                    controller: 'BwCustomerDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('bwCustomer');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'BwCustomer', function($stateParams, BwCustomer) {
                    return BwCustomer.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'bw-customer',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('bw-customer-detail.edit', {
            parent: 'bw-customer-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-customer/bw-customer-dialog.html',
                    controller: 'BwCustomerDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BwCustomer', function(BwCustomer) {
                            return BwCustomer.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('bw-customer.new', {
            parent: 'bw-customer',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-customer/bw-customer-dialog.html',
                    controller: 'BwCustomerDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomCustomer: null,
                                prenomCustomer: null,
                                typePiece: null,
                                numeroPiece: null,
                                codePays: null,
                                msisdn: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('bw-customer', null, { reload: 'bw-customer' });
                }, function() {
                    $state.go('bw-customer');
                });
            }]
        })
        .state('bw-customer.edit', {
            parent: 'bw-customer',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-customer/bw-customer-dialog.html',
                    controller: 'BwCustomerDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BwCustomer', function(BwCustomer) {
                            return BwCustomer.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('bw-customer', null, { reload: 'bw-customer' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('bw-customer.valide', {
            parent: 'bw-customer',
            url: '/{id}/valide',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-customer/bw-customer-valide-dialog.html',
                    controller: 'BwCustomerValideController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['BwCustomer', function(BwCustomer) {
                            return BwCustomer.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('bw-customer', null, { reload: 'bw-customer' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
