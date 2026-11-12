(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TemplateMessageDetailController', TemplateMessageDetailController);

    TemplateMessageDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TemplateMessage'];

    function TemplateMessageDetailController($scope, $rootScope, $stateParams, previousState, entity, TemplateMessage) {
        var vm = this;

        vm.templateMessage = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:templateMessageUpdate', function(event, result) {
            vm.templateMessage = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
