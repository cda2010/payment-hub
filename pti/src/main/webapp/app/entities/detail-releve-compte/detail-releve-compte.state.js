(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('detail-releve-compte', {
            parent: 'entity',
            url: '/detail-releve-compte?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.detailReleveCompte.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/detail-releve-compte/detail-releve-comptes.html',
                    controller: 'DetailReleveCompteController',
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
                    $translatePartialLoader.addPart('detailReleveCompte');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('detail-releve-compte-detail', {
            parent: 'detail-releve-compte',
            url: '/detail-releve-compte/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.detailReleveCompte.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/detail-releve-compte/detail-releve-compte-detail.html',
                    controller: 'DetailReleveCompteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('detailReleveCompte');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'DetailReleveCompte', function($stateParams, DetailReleveCompte) {
                    return DetailReleveCompte.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'detail-releve-compte',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('detail-releve-compte-detail.edit', {
            parent: 'detail-releve-compte-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-releve-compte/detail-releve-compte-dialog.html',
                    controller: 'DetailReleveCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DetailReleveCompte', function(DetailReleveCompte) {
                            return DetailReleveCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('detail-releve-compte.new', {
            parent: 'detail-releve-compte',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-releve-compte/detail-releve-compte-dialog.html',
                    controller: 'DetailReleveCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                date: null,
                                etat: null,
                                montant: null,
                                soldeCourant: null,
                                soldeApres: null,
                                type: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('detail-releve-compte', null, { reload: 'detail-releve-compte' });
                }, function() {
                    $state.go('detail-releve-compte');
                });
            }]
        })
        .state('detail-releve-compte.edit', {
            parent: 'detail-releve-compte',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-releve-compte/detail-releve-compte-dialog.html',
                    controller: 'DetailReleveCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DetailReleveCompte', function(DetailReleveCompte) {
                            return DetailReleveCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('detail-releve-compte', null, { reload: 'detail-releve-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('detail-releve-compte.delete', {
            parent: 'detail-releve-compte',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-releve-compte/detail-releve-compte-delete-dialog.html',
                    controller: 'DetailReleveCompteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['DetailReleveCompte', function(DetailReleveCompte) {
                            return DetailReleveCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('detail-releve-compte', null, { reload: 'detail-releve-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
