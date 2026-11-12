(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('details-lot', {
            parent: 'entity',
            url: '/details-lot',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facturier.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/details-lot/details-lot.html',
                    controller: 'DetailsLotController',
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
                    $translatePartialLoader.addPart('details-lot');
                    // $translatePartialLoader.addPart('typeFacturier');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('details-lot-detail', {
            parent: 'details-lot',
            url: '/details-lot/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facturier.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/details-lot/details-lot-detail.html',
                    controller: 'DetailsLotDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('details-lot');
                    // $translatePartialLoader.addPart('typeFacturier');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'DetailsLot', function($stateParams, DetailsLot) {
                    return DetailsLot.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'details-lot',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('details-lot-detail.edit', {
            parent: 'details-lot-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/details-lot/details-lot-dialog.html',
                    controller: 'DetailsLotDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['DetailsLot', function(DetailsLot) {
                            return DetailsLot.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('details-lot.new', {
            parent: 'details-lot',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/details-lot/details-lot-dialog.html',
                    controller: 'DetailsLotDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('details-lot', null, { reload: 'details-lot' });
                }, function() {
                    $state.go('details-lot');
                });
            }]
            
        })
        .state('details-lot.edit', {
            parent: 'details-lot',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/details-lot/details-lot-dialog.html',
                    controller: 'DetailsLotDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['DetailsLot', function(DetailsLot) {
                            return DetailsLot.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('details-lot', null, { reload: 'details-lot' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('details-lot.delete', {
            parent: 'details-lot',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/details-lot/details-lot-delete-dialog.html',
                    controller: 'DetailsLotDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['DetailsLot', function(DetailsLot) {
                            return DetailsLot.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('details-lot', null, { reload: 'details-lot' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
