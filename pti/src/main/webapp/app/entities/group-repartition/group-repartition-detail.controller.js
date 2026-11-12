(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupRepartitionDetailController', GroupRepartitionDetailController);

    GroupRepartitionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'GroupRepartition'];

    function GroupRepartitionDetailController($scope, $rootScope, $stateParams, previousState, entity, GroupRepartition) {
        var vm = this;

        vm.groupRepartition = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:groupRepartitionUpdate', function(event, result) {
            vm.groupRepartition = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
