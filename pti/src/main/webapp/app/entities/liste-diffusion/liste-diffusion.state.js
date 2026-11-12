(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('liste-diffusion', {
            parent: 'entity',
            url: '/liste-diffusion?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.listeDiffusion.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/liste-diffusion/liste-diffusions.html',
                    controller: 'ListeDiffusionController',
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
                    $translatePartialLoader.addPart('listeDiffusion');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('liste-diffusion-detail', {
            parent: 'liste-diffusion',
            url: '/liste-diffusion/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.listeDiffusion.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/liste-diffusion/liste-diffusion-detail.html',
                    controller: 'ListeDiffusionDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('listeDiffusion');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ListeDiffusion', function($stateParams, ListeDiffusion) {
                    return ListeDiffusion.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'liste-diffusion',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('liste-diffusion-detail.edit', {
            parent: 'liste-diffusion-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/liste-diffusion/liste-diffusion-dialog.html',
                    controller: 'ListeDiffusionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['ListeDiffusion', function(ListeDiffusion) {
                            return ListeDiffusion.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('liste-diffusion.new', {
            parent: 'liste-diffusion',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/liste-diffusion/liste-diffusion-dialog.html',
                    controller: 'ListeDiffusionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                nomListe: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('liste-diffusion', null, { reload: 'liste-diffusion' });
                }, function() {
                    $state.go('liste-diffusion');
                });
            }]
        })
        .state('liste-diffusion.edit', {
            parent: 'liste-diffusion',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/liste-diffusion/liste-diffusion-dialog.html',
                    controller: 'ListeDiffusionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['ListeDiffusion', function(ListeDiffusion) {
                            return ListeDiffusion.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('liste-diffusion', null, { reload: 'liste-diffusion' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('liste-diffusion.delete', {
            parent: 'liste-diffusion',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/liste-diffusion/liste-diffusion-delete-dialog.html',
                    controller: 'ListeDiffusionDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ListeDiffusion', function(ListeDiffusion) {
                            return ListeDiffusion.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('liste-diffusion', null, { reload: 'liste-diffusion' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
