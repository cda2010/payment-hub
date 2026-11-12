(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('MethodeDetailController', MethodeDetailController);

    MethodeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Methode', 'Api', 'ParametreMethode'];

    function MethodeDetailController($scope, $rootScope, $stateParams, previousState, entity, Methode, Api, ParametreMethode) {
        var vm = this;

        vm.methode = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('exempleApp:methodeUpdate', function(event, result) {
            vm.methode = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
