(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreMethodeDetailController', ParametreMethodeDetailController);

    ParametreMethodeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ParametreMethode', 'Methode'];

    function ParametreMethodeDetailController($scope, $rootScope, $stateParams, previousState, entity, ParametreMethode, Methode) {
        var vm = this;

        vm.parametreMethode = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('exempleApp:parametreMethodeUpdate', function(event, result) {
            vm.parametreMethode = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
