(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('lot', {
            parent: 'entity',
            url: '/lot',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facturier.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/lot/lots.html',
                    controller: 'LotController',
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
                    $translatePartialLoader.addPart('lot');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        }).state('lot-organisation', {
                parent: 'entity',
                url: '/lot-organisation',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'ptiApp.facturier.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/lot/lots-type.html',
                        controller: 'LotAgentController',
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
                        $translatePartialLoader.addPart('lot');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
        .state('lot-detail', {
            parent: 'lot',
            url: '/lot/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facturier.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/lot/lot-detail.html',
                    controller: 'LotDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('lot');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Lot', function($stateParams, Lot) {
                    return Lot.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'lot',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('lot-detail.edit', {
            parent: 'lot-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/lot/lot-dialog.html',
                    controller: 'LotDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Lot', function(Lot) {
                            return Lot.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lot.new', {
            parent: 'lot',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },

            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/lot/lot-dialog.html',
                    controller: 'LotDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
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
                        entity: function () {
                            return {
                                etat: null,
                                dateOuverture: null,
                                dateFermeture: null,
                                estImprime: null,
                                dateImpression:null,
                                organisation: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('lot', null, { reload: 'lot' });
                }, function() {
                    $state.go('lot');
                });
            }]
        })
            .state('lot-organisation.new', {
                parent: 'lot-organisation',
                url: '/new',
                data: {
                    authorities: ['ROLE_USER']
                },

                onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/lot/lot-type-dialog.html',
                        controller: 'LotOrganisationDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'md',
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
                            entity: function () {
                                return {
                                    etat: null,
                                    dateOuverture: null,
                                    dateFermeture: null,
                                    estImprime: null,
                                    dateImpression:null,
                                    organisation: null,
                                    id: null
                                };
                            }
                        }
                    }).result.then(function() {
                        $state.go('lot-organisation', null, { reload: 'lot-organisation' });
                    }, function() {
                        $state.go('lot-organisation');
                    });
                }]
            })
            .state('lot.edit', {
                parent: 'lot',
                url: '/{id}/edit',
                data: {
                    authorities: ['ROLE_USER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/lot/lot-dialog.html',
                        controller: 'LotDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'md',
                        resolve: {
                            entity: ['Lot', function(Lot) {
                                return Lot.get({id : $stateParams.id}).$promise;
                            }]
                        }
                    }).result.then(function() {
                        $state.go('lot', null, { reload: 'lot' });
                    }, function() {
                        $state.go('^');
                    });
                }]
            })
            .state('lot-organisation.edit', {
            parent: 'lot-organisation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/lot/lot-type-dialog.html',
                    controller: 'LotOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Lot', function(Lot) {
                            return Lot.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lot-organisation', null, { reload: 'lot-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lot.delete', {
            parent: 'lot',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/lot/lot-delete-dialog.html',
                    controller: 'LotDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Lot', function(Lot) {
                            return Lot.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lot', null, { reload: 'lot' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
       
    }

})();
