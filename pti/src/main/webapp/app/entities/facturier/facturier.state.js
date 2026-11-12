(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('facturier', {
            parent: 'entity',
            url: '/facturier',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facturier.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/facturier/facturiers.html',
                    controller: 'FacturierController',
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
                    $translatePartialLoader.addPart('facturier');
                    $translatePartialLoader.addPart('typeFacturier');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('facturier-detail', {
            parent: 'facturier',
            url: '/facturier/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facturier.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/facturier/facturier-detail.html',
                    controller: 'FacturierDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('facturier');
                    $translatePartialLoader.addPart('typeFacturier');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Facturier', function($stateParams, Facturier) {
                    return Facturier.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'facturier',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('facturier-detail.edit', {
            parent: 'facturier-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facturier/facturier-dialog.html',
                    controller: 'FacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Facturier', function(Facturier) {
                            return Facturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('facturier.new', {
            parent: 'facturier',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facturier/facturier-dialog.html',
                    controller: 'FacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                nomFacturier: null,
                                codeFacturier: null,
                                typeFacturier: null,
                                possesseur: null,
                                classifcation:null,
                                uniteOrganisation: null,
                                secteurActivite: null,
                                fse:null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('facturier', null, { reload: 'facturier' });
                }, function() {
                    $state.go('facturier');
                });
            }]
        })
        .state('facturier.edit', {
            parent: 'facturier',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facturier/facturier-dialog.html',
                    controller: 'FacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Facturier', function(Facturier) {
                            return Facturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('facturier', null, { reload: 'facturier' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('facturier.delete', {
            parent: 'facturier',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facturier/facturier-delete-dialog.html',
                    controller: 'FacturierDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Facturier', function(Facturier) {
                            return Facturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('facturier', null, { reload: 'facturier' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
