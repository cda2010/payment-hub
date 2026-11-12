(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('champs', {
            parent: 'entity',
            url: '/champs',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.champs.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/champs/champs.html',
                    controller: 'ChampsController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('champs');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('champs-detail', {
            parent: 'champs',
            url: '/champs/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.champs.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/champs/champs-detail.html',
                    controller: 'ChampsDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('champs');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Champs', function($stateParams, Champs) {
                    return Champs.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'champs',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('champs-detail.edit', {
            parent: 'champs-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/champs/champs-dialog.html',
                    controller: 'ChampsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Champs', function(Champs) {
                            return Champs.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('champs.new', {
            parent: 'champs',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/champs/champs-dialog.html',
                    controller: 'ChampsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                aSaisir: null,
                                min: null,
                                max: null,
                                minLength: null,
                                maxLength: null,
                                isVisible: null,
                                pattern: null,
                                ecran: null,
                                id: null

                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('champs', null, { reload: 'champs' });
                }, function() {
                    $state.go('champs');
                });
            }]
        })
        .state('champs.edit', {
            parent: 'champs',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/champs/champs-dialog.html',
                    controller: 'ChampsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Champs', function(Champs) {
                            return Champs.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('champs', null, { reload: 'champs' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('champs.delete', {
            parent: 'champs',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/champs/champs-delete-dialog.html',
                    controller: 'ChampsDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Champs', function(Champs) {
                            return Champs.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('champs', null, { reload: 'champs' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
