(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('detail-liste-diffusion', {
            parent: 'entity',
            url: '/detail-liste-diffusion?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.detailListeDiffusion.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/detail-liste-diffusion/detail-liste-diffusions.html',
                    controller: 'DetailListeDiffusionController',
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
                    $translatePartialLoader.addPart('detailListeDiffusion');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('detail-liste-diffusion-detail', {
            parent: 'detail-liste-diffusion',
            url: '/detail-liste-diffusion/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.detailListeDiffusion.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/detail-liste-diffusion/detail-liste-diffusion-detail.html',
                    controller: 'DetailListeDiffusionDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('detailListeDiffusion');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'DetailListeDiffusion', function($stateParams, DetailListeDiffusion) {
                    return DetailListeDiffusion.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'detail-liste-diffusion',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('detail-liste-diffusion-detail.edit', {
            parent: 'detail-liste-diffusion-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-liste-diffusion/detail-liste-diffusion-dialog.html',
                    controller: 'DetailListeDiffusionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['DetailListeDiffusion', function(DetailListeDiffusion) {
                            return DetailListeDiffusion.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('detail-liste-diffusion.new', {
            parent: 'detail-liste-diffusion',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-liste-diffusion/detail-liste-diffusion-dialog.html',
                    controller: 'DetailListeDiffusionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                numero: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('detail-liste-diffusion', null, { reload: 'detail-liste-diffusion' });
                }, function() {
                    $state.go('detail-liste-diffusion');
                });
            }]
        })
        .state('detail-liste-diffusion.edit', {
            parent: 'detail-liste-diffusion',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-liste-diffusion/detail-liste-diffusion-dialog.html',
                    controller: 'DetailListeDiffusionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['DetailListeDiffusion', function(DetailListeDiffusion) {
                            return DetailListeDiffusion.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('detail-liste-diffusion', null, { reload: 'detail-liste-diffusion' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('detail-liste-diffusion.delete', {
            parent: 'detail-liste-diffusion',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-liste-diffusion/detail-liste-diffusion-delete-dialog.html',
                    controller: 'DetailListeDiffusionDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['DetailListeDiffusion', function(DetailListeDiffusion) {
                            return DetailListeDiffusion.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('detail-liste-diffusion', null, { reload: 'detail-liste-diffusion' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
