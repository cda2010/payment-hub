(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('etiquette-methode', {
            parent: 'entity',
            url: '/etiquette-methode',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Etiquettes Methodes'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/etiquette-methode/etiquette-methode.html',
                    controller: 'EtiquetteMethodeController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'code,asc',
                    squash: true
                }
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                    };
                }],
                // translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                //     $translatePartialLoader.addPart('billetage');
                //     $translatePartialLoader.addPart('global');
                //     return $translate.refresh();
                // }]
            }
        })
        .state('etiquette-methode-detail', {
            parent: 'etiquette-methode',
            url: '/etiquette-methode/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'EtiquetteMethode'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/etiquette-methode/etiquette-methode-detail.html',
                    controller: 'EtiquetteMethodeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'EtiquetteMethode', function($stateParams, EtiquetteMethode) {
                    return EtiquetteMethode.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'etiquetteMethode',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('etiquette-methode-detail.edit', {
            parent: 'etiquette-methode-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etiquette-methode/etiquette-methode-dialog.html',
                    controller: 'EtiquetteMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EtiquetteMethode', function(EtiquetteMethode) {
                            return EtiquetteMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('etiquette-methode.new', {
            parent: 'etiquette-methode',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etiquette-methode/etiquette-methode-dialog.html',
                    controller: 'EtiquetteMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                code: null,
                                libelle: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('etiquette-methode', null, { reload: 'etiquette-methode' });
                }, function() {
                    $state.go('etiquette-methode');
                });
            }]
        })
        .state('etiquette-methode.edit', {
            parent: 'etiquette-methode',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etiquette-methode/etiquette-methode-dialog.html',
                    controller: 'EtiquetteMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EtiquetteMethode', function(EtiquetteMethode) {
                            return EtiquetteMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('etiquette-methode', null, { reload: 'etiquette-methode' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('etiquette-methode.delete', {
            parent: 'etiquette-methode',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etiquette-methode/etiquette-methode-delete-dialog.html',
                    controller: 'EtiquetteMethodeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['EtiquetteMethode', function(EtiquetteMethode) {
                            return EtiquetteMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('etiquette-methode', null, { reload: 'etiquette-methode' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
