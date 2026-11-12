(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ligne-message', {
            parent: 'entity',
            url: '/ligne-message?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneMessage.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-message/ligne-messages.html',
                    controller: 'LigneMessageController',
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
                    $translatePartialLoader.addPart('ligneMessage');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('ligne-message-detail', {
            parent: 'ligne-message',
            url: '/ligne-message/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneMessage.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-message/ligne-message-detail.html',
                    controller: 'LigneMessageDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneMessage');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'LigneMessage', function($stateParams, LigneMessage) {
                    return LigneMessage.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ligne-message',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ligne-message-detail.edit', {
            parent: 'ligne-message-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-message/ligne-message-dialog.html',
                    controller: 'LigneMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneMessage', function(LigneMessage) {
                            return LigneMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-message.new', {
            parent: 'ligne-message',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-message/ligne-message-dialog.html',
                    controller: 'LigneMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeIndicatif: null,
                                numeroTelephone: null,
                                email: null,
                                message: null,
                                error: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ligne-message', null, { reload: 'ligne-message' });
                }, function() {
                    $state.go('ligne-message');
                });
            }]
        })
        .state('ligne-message.edit', {
            parent: 'ligne-message',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-message/ligne-message-dialog.html',
                    controller: 'LigneMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneMessage', function(LigneMessage) {
                            return LigneMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-message', null, { reload: 'ligne-message' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-message.delete', {
            parent: 'ligne-message',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-message/ligne-message-delete-dialog.html',
                    controller: 'LigneMessageDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['LigneMessage', function(LigneMessage) {
                            return LigneMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-message', null, { reload: 'ligne-message' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
